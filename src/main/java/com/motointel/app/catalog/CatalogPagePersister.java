package com.motointel.app.catalog;

import com.motointel.app.catalog.CatalogLinkExtractor.DiscoveredModel;
import com.motointel.app.domain.CatalogSource;
import com.motointel.app.domain.CatalogSourcePage;
import com.motointel.app.domain.CatalogSourceSnapshot;
import com.motointel.app.domain.Manufacturer;
import com.motointel.app.domain.MotorcycleModel;
import com.motointel.app.domain.MotorcycleSpec;
import com.motointel.app.domain.MotorcycleVariant;
import com.motointel.app.domain.SpecEvidence;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.repo.CatalogSourcePageRepository;
import com.motointel.app.repo.CatalogSourceSnapshotRepository;
import com.motointel.app.repo.ManufacturerRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.repo.MotorcycleSpecRepository;
import com.motointel.app.repo.MotorcycleVariantRepository;
import com.motointel.app.repo.SpecEvidenceRepository;
import com.motointel.app.storage.StorageService;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Persists a single fetched model page in its own transaction (snapshot-first):
 * save raw HTML -&gt; record page + snapshot -&gt; parse -&gt; upsert manufacturer/model/spec -&gt; evidence.
 * One transaction per page keeps a whole-catalog crawl durable (progress survives interruption)
 * and memory-bounded, instead of one giant transaction for thousands of pages.
 */
@Component
public class CatalogPagePersister {

    /** Outcome of persisting one page. {@code softError} is non-null when the page had no spec table. */
    public record PersistResult(boolean parsed, boolean inserted, String softError) {
    }

    private final CatalogSourcePageRepository pageRepo;
    private final CatalogSourceSnapshotRepository snapshotRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final MotorcycleModelRepository modelRepo;
    private final MotorcycleSpecRepository specRepo;
    private final MotorcycleVariantRepository variantRepo;
    private final SpecEvidenceRepository evidenceRepo;
    private final CatalogParser parser;
    private final StorageService storage;
    private final Normalizer normalizer;

    public CatalogPagePersister(CatalogSourcePageRepository pageRepo, CatalogSourceSnapshotRepository snapshotRepo,
                                ManufacturerRepository manufacturerRepo, MotorcycleModelRepository modelRepo,
                                MotorcycleSpecRepository specRepo, MotorcycleVariantRepository variantRepo,
                                SpecEvidenceRepository evidenceRepo, CatalogParser parser, StorageService storage,
                                Normalizer normalizer) {
        this.pageRepo = pageRepo;
        this.snapshotRepo = snapshotRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.modelRepo = modelRepo;
        this.specRepo = specRepo;
        this.variantRepo = variantRepo;
        this.evidenceRepo = evidenceRepo;
        this.parser = parser;
        this.storage = storage;
        this.normalizer = normalizer;
    }

    @Transactional
    public PersistResult persistModelPage(CatalogSource source, DiscoveredModel dm, Document doc, int httpStatus) {
        String url = dm.url();

        // snapshot-first: persist the raw page before parsing
        String html = doc.outerHtml();
        String hash = StorageService.sha256(html);
        String ref = "catalog/" + safe(source.getName()) + "/" + hash.substring(0, 12) + ".html";
        storage.saveText(ref, html);

        CatalogSourcePage page = pageRepo.findByUrl(url).orElseGet(() -> {
            CatalogSourcePage p = new CatalogSourcePage();
            p.setSourceId(source.getId());
            p.setUrl(url);
            p.setPageType("model");
            p.setDiscoveredAt(Instant.now());
            return pageRepo.save(p);
        });

        CatalogSourceSnapshot snap = new CatalogSourceSnapshot();
        snap.setPageId(page.getId());
        snap.setStorageRef(ref);
        snap.setFetchedAt(Instant.now());
        snap.setHttpStatus(httpStatus);
        snap.setContentHash(hash);
        snap = snapshotRepo.save(snap);

        List<String[]> pairs = parser.extractSpecPairs(doc);
        if (pairs.isEmpty()) {
            // Page recorded (so it is not refetched), but nothing parseable to ingest.
            return new PersistResult(false, false, "No spec table found on page");
        }

        Manufacturer manufacturer = upsertManufacturer(resolveManufacturerName(dm, doc));
        MotorcycleModel model = upsertModel(manufacturer, dm, doc);

        // The source lists each production year of a model as its own page. The first page seen for a
        // model becomes its canonical spec; later year-variant pages are recorded as variants (their
        // years also widen the model range) rather than duplicated as extra specs for the same model.
        if (!specRepo.findByModelId(model.getId()).isEmpty()) {
            upsertVariant(model, dm);
            return new PersistResult(true, false, null);
        }

        SpecValues spec = parser.mapSpecs(pairs);
        MotorcycleSpec specEntity = new MotorcycleSpec();
        specEntity.setModelId(model.getId());
        specEntity.setEngineCapacityCc(spec.engineCapacityCc());
        specEntity.setPowerKw(spec.powerKw());
        specEntity.setTorqueNm(spec.torqueNm());
        specEntity.setDryWeightKg(spec.dryWeightKg());
        specEntity.setWetWeightKg(spec.wetWeightKg());
        specEntity.setSeatHeightMm(spec.seatHeightMm());
        specEntity.setFuelCapacityL(spec.fuelCapacityL());
        specEntity.setTopSpeedKmh(spec.topSpeedKmh());
        specEntity.setCooling(spec.cooling());
        specEntity.setTransmission(spec.transmission());
        specEntity.setFinalDrive(spec.finalDrive());
        specEntity.setAbs(spec.abs());
        specEntity.setElectric(spec.isElectric());
        specEntity = specRepo.save(specEntity);

        for (String[] p : pairs) {
            SpecEvidence ev = new SpecEvidence();
            ev.setSpecId(specEntity.getId());
            ev.setSnapshotId(snap.getId());
            ev.setFieldName(p[0]);
            ev.setRawValue(p[1]);
            evidenceRepo.save(ev);
        }
        return new PersistResult(true, true, null);
    }

    private String resolveManufacturerName(DiscoveredModel dm, Document doc) {
        String name = dm.manufacturerName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        // Fallback: first token of the page title (e.g. "2012 BMW G 650GS" -> "BMW" after a leading year).
        String[] makeModel = splitMakeModel(doc.title());
        return makeModel[0];
    }

    private Manufacturer upsertManufacturer(String name) {
        String norm = normalizer.normalizeText(name);
        if (norm.isBlank()) {
            norm = "unknown";
            name = "Unknown";
        }
        final String fname = name;
        final String fnorm = norm;
        return manufacturerRepo.findByNormalizedName(fnorm).orElseGet(() -> {
            Manufacturer m = new Manufacturer();
            m.setName(fname);
            m.setNormalizedName(fnorm);
            m.setCreatedAt(Instant.now());
            return manufacturerRepo.save(m);
        });
    }

    private MotorcycleModel upsertModel(Manufacturer manufacturer, DiscoveredModel dm, Document doc) {
        String name = dm.modelName();
        if (name == null || name.isBlank()) {
            name = splitMakeModel(doc.title())[1];
        }
        String norm = normalizer.compact(name);
        if (norm.isBlank()) {
            norm = "model" + System.identityHashCode(dm.url());
        }
        final String fname = name;
        final String fnorm = norm;
        MotorcycleModel model = modelRepo.findByManufacturerIdAndNormalizedName(manufacturer.getId(), fnorm)
                .orElseGet(() -> {
                    MotorcycleModel m = new MotorcycleModel();
                    m.setManufacturerId(manufacturer.getId());
                    m.setName(fname);
                    m.setNormalizedName(fnorm);
                    m.setCreatedAt(Instant.now());
                    m.setUpdatedAt(Instant.now());
                    return modelRepo.save(m);
                });
        // Widen the production range to span every year-variant page seen for this model.
        boolean changed = false;
        Integer newStart = minIgnoringNull(model.getProductionStartYear(), dm.startYear());
        if (!equalsNullable(newStart, model.getProductionStartYear())) {
            model.setProductionStartYear(newStart);
            changed = true;
        }
        Integer newEnd = maxIgnoringNull(model.getProductionEndYear(), dm.endYear());
        if (!equalsNullable(newEnd, model.getProductionEndYear())) {
            model.setProductionEndYear(newEnd);
            changed = true;
        }
        if (changed) {
            model.setUpdatedAt(Instant.now());
            model = modelRepo.save(model);
        }
        return model;
    }

    /** Records a year-variant page as a variant row (deduped by year range), if it adds anything new. */
    private void upsertVariant(MotorcycleModel model, DiscoveredModel dm) {
        Integer from = dm.startYear();
        Integer to = dm.endYear();
        if (from == null && to == null) {
            return; // nothing distinguishing to record
        }
        boolean exists = variantRepo.findByModelId(model.getId()).stream()
                .anyMatch(v -> equalsNullable(v.getYearFrom(), from) && equalsNullable(v.getYearTo(), to));
        if (exists) {
            return;
        }
        MotorcycleVariant variant = new MotorcycleVariant();
        variant.setModelId(model.getId());
        variant.setName(model.getName() + " (" + yearLabel(from, to) + ")");
        variant.setYearFrom(from);
        variant.setYearTo(to);
        variantRepo.save(variant);
    }

    private static String yearLabel(Integer from, Integer to) {
        if (from != null && to != null) {
            return from + "-" + to;
        }
        return String.valueOf(from != null ? from : to);
    }

    private static Integer minIgnoringNull(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return Math.min(a, b);
    }

    private static Integer maxIgnoringNull(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return Math.max(a, b);
    }

    private static boolean equalsNullable(Integer a, Integer b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String[] splitMakeModel(String title) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) {
            return new String[]{"Unknown", "Unknown Model"};
        }
        int idx = t.indexOf(' ');
        if (idx <= 0) {
            return new String[]{t, t};
        }
        return new String[]{t.substring(0, idx), t.substring(idx + 1)};
    }

    private static String safe(String s) {
        return s == null ? "unknown" : s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
}
