package com.motointel.app.catalog;

import com.motointel.app.catalog.CatalogLinkExtractor.DiscoveredModel;
import com.motointel.app.catalog.CatalogLinkExtractor.ManufacturerLink;
import com.motointel.app.catalog.CatalogPagePersister.PersistResult;
import com.motointel.app.config.AppProperties;
import com.motointel.app.config.AsyncConfig;
import com.motointel.app.domain.CatalogSource;
import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.JobVocab;
import com.motointel.app.jobs.JobCancellationRegistry;
import com.motointel.app.jobs.JobService;
import com.motointel.app.repo.CatalogSourcePageRepository;
import com.motointel.app.repo.CatalogSourceRepository;
import com.motointel.app.repo.IngestionJobRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runs the catalog crawl asynchronously (off the HTTP request thread), mirroring the original tool:
 * <ol>
 *   <li>discover every manufacturer from the home page;</li>
 *   <li>for each manufacturer, page through its listing and collect every model link;</li>
 *   <li>fetch + ingest only models that are <b>not already known</b> (their page URL is absent),
 *       so the first run pulls the whole catalog and later runs pull only newly-added models.</li>
 * </ol>
 * Each model page is persisted in its own transaction (see {@link CatalogPagePersister}); a failure
 * on any page is recorded as a job error and the crawl continues.
 */
@Service
public class CatalogCrawler {

    private static final Logger log = LoggerFactory.getLogger(CatalogCrawler.class);

    private final CatalogSourceRepository sourceRepo;
    private final CatalogSourcePageRepository pageRepo;
    private final IngestionJobRepository jobRepo;
    private final CatalogLinkExtractor extractor;
    private final CatalogPagePersister persister;
    private final JobService jobService;
    private final JobCancellationRegistry cancellationRegistry;
    private final AppProperties props;

    public CatalogCrawler(CatalogSourceRepository sourceRepo, CatalogSourcePageRepository pageRepo,
                          IngestionJobRepository jobRepo, CatalogLinkExtractor extractor,
                          CatalogPagePersister persister, JobService jobService, JobCancellationRegistry cancellationRegistry, AppProperties props) {
        this.sourceRepo = sourceRepo;
        this.pageRepo = pageRepo;
        this.jobRepo = jobRepo;
        this.extractor = extractor;
        this.persister = persister;
        this.jobService = jobService;
        this.cancellationRegistry = cancellationRegistry;
        this.props = props;
    }

    @Async(AsyncConfig.CATALOG_EXECUTOR)
    public void runAsync(Long jobId, Long sourceId) {
        IngestionJob job = jobRepo.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("Catalog crawl: job {} not found, aborting", jobId);
            return;
        }
        CatalogSource source = sourceRepo.findById(sourceId).orElse(null);
        if (source == null) {
            jobService.addError(job, null, "discover", "Catalog source not found: " + sourceId, null);
            jobService.finish(job, JobVocab.STATUS_FAILED);
            return;
        }

        boolean hadError = false;
        int maxModels = props.getScraper().getMaxModelsPerRun();
        Set<String> seenThisRun = new HashSet<>();

        try {
            String base = source.getBaseUrl();
            Document home = fetch(base).doc();
            List<ManufacturerLink> manufacturers = extractor.manufacturerLinks(home);
            log.info("Catalog crawl: {} manufacturers discovered from {}", manufacturers.size(), base);
            if (manufacturers.isEmpty()) {
                jobService.addError(job, base, "discover", "No manufacturer links found on the index page", null);
                hadError = true;
            }

            outer:
            for (ManufacturerLink manufacturer : manufacturers) {
                if (cancellationRegistry.isCancelled(jobId)) {
                    log.info("Catalog crawl: cancellation requested, stopping before manufacturer {}", manufacturer.name());
                    jobService.save(job);
                    jobService.finish(job, JobVocab.STATUS_CANCELLED);
                    return;
                }
                List<DiscoveredModel> models;
                try {
                    models = collectModels(manufacturer);
                } catch (RuntimeException e) {
                    hadError = true;
                    jobService.addError(job, manufacturer.url(), "discover",
                            "Failed to list models for manufacturer " + manufacturer.name(), e.getMessage());
                    continue;
                }

                for (DiscoveredModel dm : models) {
                    if (cancellationRegistry.isCancelled(jobId)) {
                        log.info("Catalog crawl: cancellation requested, stopping at model {}", dm.url());
                        jobService.save(job);
                        jobService.finish(job, JobVocab.STATUS_CANCELLED);
                        return;
                    }
                    if (!seenThisRun.add(dm.url())) {
                        continue; // same model linked under two manufacturers in this run
                    }
                    job.setDiscovered(job.getDiscovered() + 1);

                    // Incremental: a model whose page URL already exists was ingested before -> skip (no fetch).
                    if (pageRepo.findByUrl(dm.url()).isPresent()) {
                        continue;
                    }
                    if (maxModels > 0 && job.getInserted() >= maxModels) {
                        log.info("Catalog crawl: reached max-models-per-run={}, stopping early", maxModels);
                        break outer;
                    }

                    try {
                        FetchedPage page = fetch(dm.url());
                        job.setFetched(job.getFetched() + 1);
                        PersistResult result = persister.persistModelPage(source, dm, page.doc(), page.status());
                        if (result.parsed()) {
                            job.setParsed(job.getParsed() + 1);
                        }
                        if (result.inserted()) {
                            job.setInserted(job.getInserted() + 1);
                        } else if (result.softError() != null) {
                            hadError = true;
                            jobService.addError(job, dm.url(), "parse", result.softError(), null);
                        }
                    } catch (RuntimeException e) {
                        hadError = true;
                        jobService.addError(job, dm.url(), "fetch", "Failed to ingest model page", e.getMessage());
                    }
                    rateLimit();
                }
                jobService.save(job); // checkpoint progress after each manufacturer
            }
        } catch (Exception e) {
            hadError = true;
            jobService.addError(job, source.getBaseUrl(), "fetch",
                    "Could not reach catalog source", e.getMessage());
            log.info("Catalog crawl could not reach {}: {}", source.getBaseUrl(), e.getMessage());
        }

        source.setLastRunAt(Instant.now());
        sourceRepo.save(source);
        if (hadError) {
            job.setErrorSummary(summarize(job));
        }
        jobService.finish(job, hadError ? JobVocab.STATUS_COMPLETED_WITH_ERRORS : JobVocab.STATUS_COMPLETED);
        log.info("Catalog crawl finished: discovered={} fetched={} parsed={} inserted={} failed={}",
                job.getDiscovered(), job.getFetched(), job.getParsed(), job.getInserted(), job.getFailed());
    }

    /** Walk a manufacturer's listing pages (following "Next") and collect every unique model link. */
    private List<DiscoveredModel> collectModels(ManufacturerLink manufacturer) {
        List<DiscoveredModel> all = new ArrayList<>();
        Set<String> modelUrls = new HashSet<>();
        Set<String> visitedPages = new HashSet<>();
        int maxPages = Math.max(1, props.getScraper().getMaxPagesPerManufacturer());
        String pageUrl = manufacturer.url();
        int pages = 0;
        while (pageUrl != null && visitedPages.add(pageUrl) && pages < maxPages) {
            pages++;
            Document doc = fetch(pageUrl).doc();
            for (DiscoveredModel dm : extractor.modelLinks(doc, manufacturer.name())) {
                if (modelUrls.add(dm.url())) {
                    all.add(dm);
                }
            }
            pageUrl = extractor.nextPageUrl(doc).orElse(null);
            if (pageUrl != null) {
                rateLimit(); // polite between manufacturer listing pages too
            }
        }
        return all;
    }

    private FetchedPage fetch(String url) {
        try {
            Connection.Response resp = Jsoup.connect(url)
                    .userAgent(props.getScraper().getUserAgent())
                    .timeout(props.getScraper().getHttpTimeoutMs())
                    .ignoreHttpErrors(true)
                    .followRedirects(true)
                    .maxBodySize(0)
                    .execute();
            return new FetchedPage(resp.parse(), resp.statusCode());
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

    private static String summarize(IngestionJob job) {
        return "discovered=" + job.getDiscovered() + " fetched=" + job.getFetched()
                + " parsed=" + job.getParsed() + " inserted=" + job.getInserted()
                + " errors=" + job.getFailed();
    }

    private record FetchedPage(Document doc, int status) {
    }
}
