package com.motointel.app.scheduler;

import com.motointel.app.catalog.CatalogScraperService;
import com.motointel.app.domain.CatalogSource;
import com.motointel.app.repo.CatalogSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodic ingestion trigger. Fires on {@code app.scheduler.default-cron} (03:00 daily by default)
 * but only acts on ENABLED sources — so scheduling is effectively disabled until a source is enabled.
 */
@Component
public class IngestionScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

    private final CatalogSourceRepository catalogSourceRepo;
    private final CatalogScraperService catalogScraper;

    public IngestionScheduler(CatalogSourceRepository catalogSourceRepo, CatalogScraperService catalogScraper) {
        this.catalogSourceRepo = catalogSourceRepo;
        this.catalogScraper = catalogScraper;
    }

    @Scheduled(cron = "${app.scheduler.default-cron}")
    public void runScheduledIngestion() {
        for (CatalogSource source : catalogSourceRepo.findByEnabledTrue()) {
            try {
                log.info("Scheduled catalog ingest for source {}", source.getName());
                catalogScraper.ingest(source.getId());
            } catch (RuntimeException e) {
                log.warn("Scheduled ingest failed for {}: {}", source.getName(), e.getMessage());
            }
        }
    }
}
