package com.motointel.app.imports;

import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.JobVocab;
import com.motointel.app.domain.ListingPriceHistory;
import com.motointel.app.domain.ListingStatus;
import com.motointel.app.domain.ListingStatusHistory;
import com.motointel.app.domain.Manufacturer;
import com.motointel.app.domain.MarketListing;
import com.motointel.app.domain.MarketListingImage;
import com.motointel.app.domain.MarketListingSnapshot;
import com.motointel.app.domain.MarketSource;
import com.motointel.app.dto.ImportResultDto;
import com.motointel.app.jobs.JobService;
import com.motointel.app.lifecycle.ListingLifecycleService;
import com.motointel.app.matching.CandidateModel;
import com.motointel.app.matching.MatchService;
import com.motointel.app.normalize.Normalizer;
import com.motointel.app.repo.ListingPriceHistoryRepository;
import com.motointel.app.repo.ListingStatusHistoryRepository;
import com.motointel.app.repo.ManufacturerRepository;
import com.motointel.app.repo.MarketListingImageRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MarketListingSnapshotRepository;
import com.motointel.app.repo.MarketSourceRepository;
import com.motointel.app.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Orchestrates CSV import: parse -> upsert (dedupe) -> price/snapshot -> lifecycle -> auto-match. */
@Service
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

    private final CsvListingParser parser;
    private final MarketListingRepository listingRepo;
    private final ListingPriceHistoryRepository priceRepo;
    private final ListingStatusHistoryRepository statusRepo;
    private final MarketListingSnapshotRepository snapshotRepo;
    private final MarketListingImageRepository imageRepo;
    private final MarketSourceRepository marketSourceRepo;
    private final ManufacturerRepository manufacturerRepo;
    private final ListingLifecycleService lifecycle;
    private final MatchService matchService;
    private final JobService jobService;
    private final Normalizer normalizer;
    private final StorageService storage;

    public CsvImportService(CsvListingParser parser, MarketListingRepository listingRepo,
                            ListingPriceHistoryRepository priceRepo, ListingStatusHistoryRepository statusRepo,
                            MarketListingSnapshotRepository snapshotRepo, MarketListingImageRepository imageRepo,
                            MarketSourceRepository marketSourceRepo, ManufacturerRepository manufacturerRepo,
                            ListingLifecycleService lifecycle, MatchService matchService, JobService jobService,
                            Normalizer normalizer, StorageService storage) {
        this.parser = parser;
        this.listingRepo = listingRepo;
        this.priceRepo = priceRepo;
        this.statusRepo = statusRepo;
        this.snapshotRepo = snapshotRepo;
        this.imageRepo = imageRepo;
        this.marketSourceRepo = marketSourceRepo;
        this.manufacturerRepo = manufacturerRepo;
        this.lifecycle = lifecycle;
        this.matchService = matchService;
        this.jobService = jobService;
        this.normalizer = normalizer;
        this.storage = storage;
    }

    @Transactional
    public ImportResultDto importCsv(String content) {
        CsvParseResult parsed = parser.parse(content);
        List<ImportResultDto.RowError> rowErrors = new ArrayList<>(parsed.errors());

        Long sourceId = parsed.rows().stream().findFirst()
                .flatMap(r -> marketSourceRepo.findByName(r.source()).map(MarketSource::getId))
                .map(com.motointel.app.sources.SourceIds::unifiedMarket)
                .orElse(null);
        IngestionJob job = jobService.start(JobVocab.TYPE_CSV_IMPORT, sourceId);

        List<CandidateModel> candidates = matchService.loadCandidates();
        Map<String, String> knownManufacturers = manufacturerRepo.findAll().stream()
                .collect(Collectors.toMap(Manufacturer::getNormalizedName, Manufacturer::getName, (a, b) -> a));

        int inserted = 0;
        int updated = 0;
        Set<Integer> failedRows = parsed.errors().stream()
                .map(ImportResultDto.RowError::row).filter(r -> r > 0).collect(Collectors.toCollection(HashSet::new));

        for (ParsedRow row : parsed.rows()) {
            try {
                UpsertResult res = upsert(row, knownManufacturers);
                if (res.inserted) inserted++; else updated++;
                matchService.runAndPersist(res.listing, candidates);
            } catch (RuntimeException e) {
                log.warn("Row {} failed to import: {}", row.rowNumber(), e.getMessage());
                rowErrors.add(new ImportResultDto.RowError(row.rowNumber(), "persist", e.getMessage()));
                failedRows.add(row.rowNumber());
            }
        }

        int failed = failedRows.size();
        job.setDiscovered(parsed.rows().size() + parsed.errors().size());
        job.setFetched(parsed.rows().size());
        job.setParsed(parsed.rows().size());
        job.setInserted(inserted);
        job.setUpdated(updated);
        job.setFailed(failed);
        if (failed > 0) {
            job.setErrorSummary(failed + " row(s) had validation/import errors");
        }
        jobService.finish(job, failed > 0 ? JobVocab.STATUS_COMPLETED_WITH_ERRORS : JobVocab.STATUS_COMPLETED);

        return new ImportResultDto(job.getId(), inserted, updated, failed, rowErrors);
    }

    private UpsertResult upsert(ParsedRow row, Map<String, String> knownManufacturers) {
        Optional<MarketListing> existingOpt = row.externalId() != null
                ? listingRepo.findBySourceAndExternalId(row.source(), row.externalId())
                : listingRepo.findBySourceAndNormalizedUrl(row.source(), row.normalizedUrl());

        boolean isNew = existingOpt.isEmpty();
        MarketListing listing = existingOpt.orElseGet(MarketListing::new);

        if (isNew) {
            listing.setSource(row.source());
            listing.setExternalId(row.externalId());
            listing.setUrl(row.url());
            listing.setNormalizedUrl(row.normalizedUrl());
            listing.setFirstObservedAt(row.observedAt());
            listing.setLastObservedAt(row.observedAt());
            listing.setStatus(ListingStatus.ACTIVE);
            listing.setConsecutiveMissingCount(0);
            listing.setMarketSourceId(marketSourceRepo.findByName(row.source()).map(MarketSource::getId).orElse(null));
        }

        // Mutable fields (latest wins)
        listing.setTitle(row.title());
        listing.setDescription(row.description());
        listing.setCurrency(row.currency());
        listing.setLocation(row.location());
        listing.setSellerType(row.sellerType());
        listing.setPostedDate(row.postedDate());
        applyExtraction(listing, row, knownManufacturers);

        BigDecimal oldPrice = listing.getPrice();
        listing.setPrice(row.price());

        if (isNew) {
            listing = listingRepo.save(listing);
            // initial active history row
            ListingStatusHistory h = new ListingStatusHistory();
            h.setListingId(listing.getId());
            h.setStatus(ListingStatus.ACTIVE);
            h.setAt(row.observedAt());
            statusRepo.save(h);
            insertPrice(listing, row);
        } else {
            lifecycle.applyObserved(listing, row.observedAt());
            // price changed -> new price-history row
            if (oldPrice == null || row.price() == null || oldPrice.compareTo(row.price()) != 0) {
                insertPrice(listing, row);
            }
            listing = listingRepo.save(listing);
        }

        // Always insert a snapshot for the observation.
        insertSnapshot(listing, row);
        // First image, if provided and not already present.
        if (row.imageUrl() != null && imageRepo.findByListingId(listing.getId()).isEmpty()) {
            MarketListingImage img = new MarketListingImage();
            img.setListingId(listing.getId());
            img.setStorageRef(row.imageUrl());
            img.setSourceUrl(row.imageUrl());
            imageRepo.save(img);
        }
        listingRepo.save(listing);
        return new UpsertResult(listing, isNew);
    }

    private void applyExtraction(MarketListing listing, ParsedRow row, Map<String, String> knownManufacturers) {
        String text = row.title() + " " + (row.description() == null ? "" : row.description());
        normalizer.extractYear(text).ifPresent(listing::setExtractedYear);
        normalizer.capacityFromModelName(row.title())
                .ifPresent(cc -> listing.setExtractedCapacityCc(BigDecimal.valueOf(cc)));
        normalizer.detectManufacturer(text, new ArrayList<>(knownManufacturers.keySet()))
                .ifPresent(norm -> listing.setExtractedManufacturer(knownManufacturers.get(norm)));
        listing.setExtractedModelText(row.title());
        if (row.mileageKm() != null) {
            listing.setExtractedMileageKm(row.mileageKm());
        } else {
            normalizer.extractMileageKm(text).ifPresent(listing::setExtractedMileageKm);
        }
    }

    private void insertPrice(MarketListing listing, ParsedRow row) {
        if (row.price() == null) {
            return;
        }
        ListingPriceHistory ph = new ListingPriceHistory();
        ph.setListingId(listing.getId());
        ph.setPrice(row.price());
        ph.setCurrency(row.currency());
        ph.setObservedAt(row.observedAt());
        priceRepo.save(ph);
    }

    private void insertSnapshot(MarketListing listing, ParsedRow row) {
        String body = "<html><body><h1>" + escape(row.title()) + "</h1>"
                + "<p>" + escape(row.source()) + " — " + escape(row.url()) + "</p>"
                + "<p>price: " + row.price() + " " + escape(row.currency()) + "</p></body></html>";
        String hash = StorageService.sha256(body);
        String ref = "market/" + safe(row.source()) + "/" + listing.getId() + "-" + hash.substring(0, 12) + ".html";
        storage.saveText(ref, body);

        MarketListingSnapshot snap = new MarketListingSnapshot();
        snap.setListingId(listing.getId());
        snap.setStorageRef(ref);
        snap.setCapturedAt(row.observedAt() == null ? Instant.now() : row.observedAt());
        snap.setContentHash(hash);
        snap = snapshotRepo.save(snap);
        listing.setLatestSnapshotId(snap.getId());
    }

    private static String safe(String s) {
        return s == null ? "unknown" : s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private record UpsertResult(MarketListing listing, boolean inserted) {
    }
}
