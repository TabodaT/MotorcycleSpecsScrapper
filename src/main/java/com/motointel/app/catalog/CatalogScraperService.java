package com.motointel.app.catalog;

import com.motointel.app.domain.CatalogSource;
import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.JobVocab;
import com.motointel.app.jobs.JobService;
import com.motointel.app.repo.CatalogSourceRepository;
import org.springframework.stereotype.Service;

/**
 * Entry point for catalog ingestion. Starts (and commits) the job row, then hands the long-running,
 * rate-limited crawl off to {@link CatalogCrawler} on a background thread and returns immediately so
 * the HTTP request (or the scheduler) does not block for the duration of a whole-catalog crawl.
 *
 * <p>Live ingestion is best-effort and off the critical path: per-page failures are recorded as
 * {@code ingestion_job_errors} and the job ends {@code completed_with_errors}; the app never crashes.
 */
@Service
public class CatalogScraperService {

    private final CatalogSourceRepository sourceRepo;
    private final CatalogCrawler crawler;
    private final JobService jobService;

    public CatalogScraperService(CatalogSourceRepository sourceRepo, CatalogCrawler crawler, JobService jobService) {
        this.sourceRepo = sourceRepo;
        this.crawler = crawler;
        this.jobService = jobService;
    }

    /**
     * Starts a catalog ingest. Returns the {@code running} job immediately; the crawl proceeds
     * asynchronously and transitions the job to {@code completed}/{@code completed_with_errors}.
     */
    public IngestionJob ingest(Long catalogSourceId) {
        CatalogSource source = sourceRepo.findById(catalogSourceId).orElse(null);
        IngestionJob job = jobService.start(JobVocab.TYPE_CATALOG_INGEST, catalogSourceId);
        if (source == null) {
            jobService.addError(job, null, "discover", "Catalog source not found: " + catalogSourceId, null);
            return jobService.finish(job, JobVocab.STATUS_FAILED);
        }
        // jobService.start committed the job (its own transaction) so the async worker can load it.
        crawler.runAsync(job.getId(), source.getId());
        return job;
    }
}
