package com.motointel.app.sources;

import com.motointel.app.catalog.CatalogScraperService;
import com.motointel.app.common.ApiException;
import com.motointel.app.domain.CatalogSource;
import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.JobVocab;
import com.motointel.app.domain.MarketSource;
import com.motointel.app.dto.IngestStartedDto;
import com.motointel.app.dto.SourceDto;
import com.motointel.app.jobs.JobService;
import com.motointel.app.repo.CatalogSourceRepository;
import com.motointel.app.repo.MarketSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Unified view + actions over catalog and market sources (§4 /api/sources). */
@Service
public class SourceService {

    private final CatalogSourceRepository catalogRepo;
    private final MarketSourceRepository marketRepo;
    private final CatalogScraperService catalogScraper;
    private final JobService jobService;

    public SourceService(CatalogSourceRepository catalogRepo, MarketSourceRepository marketRepo,
                         CatalogScraperService catalogScraper, JobService jobService) {
        this.catalogRepo = catalogRepo;
        this.marketRepo = marketRepo;
        this.catalogScraper = catalogScraper;
        this.jobService = jobService;
    }

    @Transactional(readOnly = true)
    public List<SourceDto> list() {
        List<SourceDto> all = new ArrayList<>();
        for (CatalogSource c : catalogRepo.findAll()) {
            all.add(toDto(SourceIds.unifiedCatalog(c.getId()), c.getName(), c.getSourceType(),
                    c.getAccessMethod(), c.getBaseUrl(), c.isEnabled(), c.getComplianceStatus(),
                    c.getScheduleCron(), c));
        }
        for (MarketSource m : marketRepo.findAll()) {
            all.add(new SourceDto(SourceIds.unifiedMarket(m.getId()), m.getName(), m.getSourceType(),
                    m.getAccessMethod(), m.getBaseUrl(), m.isEnabled(), m.getComplianceStatus(),
                    m.getScheduleCron(), m.getLastRunAt(), m.getNextRunAt()));
        }
        all.sort(Comparator.comparing(SourceDto::name, String.CASE_INSENSITIVE_ORDER));
        return all;
    }

    // Intentionally NOT @Transactional: jobService.start() must commit the job row in its own
    // transaction before CatalogCrawler.runAsync() loads it on a background thread.
    public IngestStartedDto ingest(long unifiedId) {
        if (SourceIds.isMarket(unifiedId)) {
            long marketId = SourceIds.marketId(unifiedId);
            MarketSource source = marketRepo.findById(marketId)
                    .orElseThrow(() -> ApiException.notFound("Source not found: " + unifiedId));
            // Live market ingestion is disabled by design; CSV import is the supported path (§9).
            IngestionJob job = jobService.start(JobVocab.TYPE_MARKET_INGEST, unifiedId);
            jobService.addError(job, source.getBaseUrl(), "policy",
                    "Live ingestion is disabled for this market source; use CSV import.", null);
            job.setErrorSummary("Live ingestion disabled — use CSV import.");
            jobService.finish(job, JobVocab.STATUS_COMPLETED_WITH_ERRORS);
            return new IngestStartedDto(job.getId());
        }
        CatalogSource source = catalogRepo.findById(unifiedId)
                .orElseThrow(() -> ApiException.notFound("Source not found: " + unifiedId));
        IngestionJob job = catalogScraper.ingest(source.getId());
        return new IngestStartedDto(job.getId());
    }

    @Transactional
    public SourceDto patch(long unifiedId, Boolean enabled, String scheduleCron) {
        if (SourceIds.isMarket(unifiedId)) {
            MarketSource m = marketRepo.findById(SourceIds.marketId(unifiedId))
                    .orElseThrow(() -> ApiException.notFound("Source not found: " + unifiedId));
            if (enabled != null) m.setEnabled(enabled);
            if (scheduleCron != null) m.setScheduleCron(scheduleCron);
            marketRepo.save(m);
            return new SourceDto(unifiedId, m.getName(), m.getSourceType(), m.getAccessMethod(), m.getBaseUrl(),
                    m.isEnabled(), m.getComplianceStatus(), m.getScheduleCron(), m.getLastRunAt(), m.getNextRunAt());
        }
        CatalogSource c = catalogRepo.findById(unifiedId)
                .orElseThrow(() -> ApiException.notFound("Source not found: " + unifiedId));
        if (enabled != null) c.setEnabled(enabled);
        if (scheduleCron != null) c.setScheduleCron(scheduleCron);
        catalogRepo.save(c);
        return toDto(unifiedId, c.getName(), c.getSourceType(), c.getAccessMethod(), c.getBaseUrl(),
                c.isEnabled(), c.getComplianceStatus(), c.getScheduleCron(), c);
    }

    private SourceDto toDto(long id, String name, String type, String access, String baseUrl,
                            boolean enabled, String compliance, String cron, CatalogSource c) {
        return new SourceDto(id, name, type, access, baseUrl, enabled, compliance, cron,
                c.getLastRunAt(), c.getNextRunAt());
    }
}
