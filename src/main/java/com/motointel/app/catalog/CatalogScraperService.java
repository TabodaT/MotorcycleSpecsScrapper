package com.motointel.app.catalog;

import com.motointel.app.config.AppProperties;
import com.motointel.app.domain.CatalogSource;
import com.motointel.app.domain.CatalogSourcePage;
import com.motointel.app.domain.CatalogSourceSnapshot;
import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.JobVocab;
import com.motointel.app.domain.Manufacturer;
import com.motointel.app.domain.MotorcycleModel;
import com.motointel.app.domain.MotorcycleSpec;
import com.motointel.app.domain.SpecEvidence;
import com.motointel.app.jobs.JobService;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.repo.CatalogSourcePageRepository;
import com.motointel.app.repo.CatalogSourceRepository;
import com.motointel.app.repo.CatalogSourceSnapshotRepository;
import com.motointel.app.repo.ManufacturerRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import com.motointel.app.repo.MotorcycleSpecRepository;
import com.motointel.app.repo.SpecEvidenceRepository;
import com.motointel.app.storage.StorageService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Best-effort catalog ingestion via jsoup, snapshot-first:
 * discover -&gt; fetch -&gt; save raw snapshot -&gt; parse -&gt; normalize -&gt; upsert -&gt; evidence.
 * Live ingestion is OFF the critical path: on any failure it records an
 * {@code ingestion_job_errors} row and continues; the job ends {@code completed_with_errors}.
 */
@Service
public class CatalogScraperService {

    private static final Logger log = LoggerFactory.getLogger(CatalogScraperService.class);
    private static final int MAX_PAGES = 8;

    private final CatalogSourceRepository sourceRepo;
    private final CatalogSourcePageRepository pageRepo;
    private final CatalogSourceSnapshotRepository snapshotRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final MotorcycleModelRepository modelRepo;
    private final MotorcycleSpecRepository specRepo;
    private final SpecEvidenceRepository evidenceRepo;
    private final CatalogParser parser;
    private final JobService jobService;
    private final StorageService storage;
    private final Normalizer normalizer;
    private final AppProperties props;

    public CatalogScraperService(CatalogSourceRepository sourceRepo, CatalogSourcePageRepository pageRepo,
                                 CatalogSourceSnapshotRepository snapshotRepo, ManufacturerRepository manufacturerRepo,
                                 MotorcycleModelRepository modelRepo, MotorcycleSpecRepository specRepo,
                                 SpecEvidenceRepository evidenceRepo, CatalogParser parser, JobService jobService,
                                 StorageService storage, Normalizer normalizer, AppProperties props) {
        this.sourceRepo = sourceRepo;
        this.pageRepo = pageRepo;
        this.snapshotRepo = snapshotRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.modelRepo = modelRepo;
        this.specRepo = specRepo;
        this.evidenceRepo = evidenceRepo;
        this.parser = parser;
        this.jobService = jobService;
        this.storage = storage;
        this.normalizer = normalizer;
        this.props = props;
    }

    @Transactional
    public IngestionJob ingest(Long catalogSourceId) {
        CatalogSource source = sourceRepo.findById(catalogSourceId).orElse(null);
        IngestionJob job = jobService.start(JobVocab.TYPE_CATALOG_INGEST, catalogSourceId);
        boolean hadError = false;

        if (source == null) {
            jobService.addError(job, null, "discover", "Catalog source not found: " + catalogSourceId, null);
            return jobService.finish(job, JobVocab.STATUS_FAILED);
        }

        try {
            String base = source.getBaseUrl();
            Document index = fetch(base);
            List<String> modelUrls = discoverModelLinks(index, base);
            job.setDiscovered(modelUrls.size());

            int processed = 0;
            for (String url : modelUrls) {
                if (processed >= MAX_PAGES) {
                    break;
                }
                processed++;
                try {
                    ingestModelPage(source, job, url);
                } catch (RuntimeException e) {
                    hadError = true;
                    jobService.addError(job, url, "parse", "Failed to ingest model page", e.getMessage());
                }
                rateLimit();
            }
        } catch (Exception e) {
            hadError = true;
            jobService.addError(job, source.getBaseUrl(), "fetch",
                    "Could not reach catalog source (best-effort; use seed data)", e.getMessage());
            log.info("Catalog ingest could not reach {}: {}", source.getBaseUrl(), e.getMessage());
        }

        source.setLastRunAt(Instant.now());
        sourceRepo.save(source);
        return jobService.finish(job, hadError ? JobVocab.STATUS_COMPLETED_WITH_ERRORS : JobVocab.STATUS_COMPLETED);
    }

    private void ingestModelPage(CatalogSource source, IngestionJob job, String url) {
        Document doc = fetch(url);
        job.setFetched(job.getFetched() + 1);

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
        snap.setHttpStatus(200);
        snap.setContentHash(hash);
        snap = snapshotRepo.save(snap);

        List<String[]> pairs = parser.extractSpecPairs(doc);
        job.setParsed(job.getParsed() + 1);
        if (pairs.isEmpty()) {
            jobService.addError(job, url, "parse", "No spec table found on page", null);
            return;
        }
        SpecValues spec = parser.mapSpecs(pairs);

        String title = doc.title();
        String[] makeModel = splitMakeModel(title);
        Manufacturer manufacturer = upsertManufacturer(makeModel[0]);
        MotorcycleModel model = upsertModel(manufacturer, makeModel[1]);

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
        job.setInserted(job.getInserted() + 1);

        for (String[] p : pairs) {
            SpecEvidence ev = new SpecEvidence();
            ev.setSpecId(specEntity.getId());
            ev.setSnapshotId(snap.getId());
            ev.setFieldName(p[0]);
            ev.setRawValue(p[1]);
            evidenceRepo.save(ev);
        }
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

    private MotorcycleModel upsertModel(Manufacturer manufacturer, String name) {
        String norm = normalizer.compact(name);
        if (norm.isBlank()) {
            norm = "model" + System.identityHashCode(name);
        }
        final String fnorm = norm;
        return modelRepo.findByManufacturerIdAndNormalizedName(manufacturer.getId(), fnorm).orElseGet(() -> {
            MotorcycleModel m = new MotorcycleModel();
            m.setManufacturerId(manufacturer.getId());
            m.setName(name);
            m.setNormalizedName(fnorm);
            m.setCreatedAt(Instant.now());
            m.setUpdatedAt(Instant.now());
            return modelRepo.save(m);
        });
    }

    private List<String> discoverModelLinks(Document index, String base) {
        Set<String> urls = new LinkedHashSet<>();
        for (Element a : index.select("a[href]")) {
            String href = a.absUrl("href");
            if (href.isBlank()) {
                href = a.attr("href");
            }
            if (href.contains("/model/") || href.toLowerCase().endsWith(".htm") || href.toLowerCase().endsWith(".html")) {
                urls.add(href);
            }
            if (urls.size() >= MAX_PAGES * 3) {
                break;
            }
        }
        return new ArrayList<>(urls);
    }

    private Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(props.getScraper().getUserAgent())
                    .timeout(props.getScraper().getHttpTimeoutMs())
                    .ignoreHttpErrors(true)
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("fetch failed: " + e.getMessage(), e);
        }
    }

    private void rateLimit() {
        int rps = Math.max(1, props.getScraper().getRateLimitRps());
        try {
            Thread.sleep(1000L / rps);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
