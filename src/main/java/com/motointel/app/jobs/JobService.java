package com.motointel.app.jobs;

import com.motointel.app.common.ApiException;
import com.motointel.app.domain.CatalogSource;
import com.motointel.app.domain.IngestionJob;
import com.motointel.app.domain.IngestionJobError;
import com.motointel.app.domain.JobVocab;
import com.motointel.app.domain.MarketSource;
import com.motointel.app.dto.JobDto;
import com.motointel.app.dto.JobErrorDto;
import com.motointel.app.repo.CatalogSourceRepository;
import com.motointel.app.repo.IngestionJobErrorRepository;
import com.motointel.app.repo.IngestionJobRepository;
import com.motointel.app.repo.MarketSourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class JobService {

    private final IngestionJobRepository jobRepo;
    private final IngestionJobErrorRepository errorRepo;
    private final CatalogSourceRepository catalogSourceRepo;
    private final MarketSourceRepository marketSourceRepo;

    public JobService(IngestionJobRepository jobRepo, IngestionJobErrorRepository errorRepo,
                      CatalogSourceRepository catalogSourceRepo, MarketSourceRepository marketSourceRepo) {
        this.jobRepo = jobRepo;
        this.errorRepo = errorRepo;
        this.catalogSourceRepo = catalogSourceRepo;
        this.marketSourceRepo = marketSourceRepo;
    }

    @Transactional
    public IngestionJob start(String type, Long sourceId) {
        IngestionJob job = new IngestionJob();
        job.setType(type);
        job.setSourceId(sourceId);
        job.setStatus(JobVocab.STATUS_RUNNING);
        job.setStartedAt(Instant.now());
        return jobRepo.save(job);
    }

    @Transactional
    public IngestionJob save(IngestionJob job) {
        return jobRepo.save(job);
    }

    @Transactional
    public void addError(IngestionJob job, String url, String stage, String message, String detail) {
        IngestionJobError err = new IngestionJobError();
        err.setJobId(job.getId());
        err.setUrl(url);
        err.setStage(stage);
        err.setMessage(message);
        err.setDetail(detail);
        err.setCreatedAt(Instant.now());
        errorRepo.save(err);
        job.setFailed(job.getFailed() + 1);
    }

    @Transactional
    public IngestionJob finish(IngestionJob job, String status) {
        job.setStatus(status);
        job.setFinishedAt(Instant.now());
        return jobRepo.save(job);
    }

    @Transactional(readOnly = true)
    public JobDto getJob(Long id) {
        IngestionJob job = jobRepo.findById(id).orElseThrow(() -> ApiException.notFound("Job not found: " + id));
        List<JobErrorDto> errors = errorRepo.findByJobIdOrderByIdAsc(id).stream().map(this::toErrorDto).toList();
        return toDto(job, errors);
    }

    @Transactional(readOnly = true)
    public Page<JobDto> search(String type, String status, int page, int size) {
        Page<IngestionJob> jobs = jobRepo.search(nullable(type), nullable(status), PageRequest.of(page, size));
        return jobs.map(j -> toDto(j, List.of()));
    }

    @Transactional(readOnly = true)
    public List<JobDto> latestJobs() {
        return jobRepo.findTop5ByOrderByStartedAtDescIdDesc().stream().map(j -> toDto(j, List.of())).toList();
    }

    @Transactional(readOnly = true)
    public List<JobErrorDto> latestErrors() {
        return errorRepo.findTop10ByOrderByCreatedAtDescIdDesc().stream().map(this::toErrorDto).toList();
    }

    public JobDto toDto(IngestionJob j, List<JobErrorDto> errors) {
        JobDto.Counts counts = new JobDto.Counts(j.getDiscovered(), j.getFetched(), j.getParsed(),
                j.getInserted(), j.getUpdated(), j.getFailed());
        return new JobDto(j.getId(), j.getType(), resolveSourceName(j.getSourceId()), j.getStatus(),
                j.getStartedAt(), j.getFinishedAt(), counts, j.getErrorSummary(), errors);
    }

    private JobErrorDto toErrorDto(IngestionJobError e) {
        return new JobErrorDto(e.getId(), e.getUrl(), e.getStage(), e.getMessage(), e.getDetail(), e.getCreatedAt());
    }

    private String resolveSourceName(Long sourceId) {
        if (sourceId == null) {
            return null;
        }
        if (com.motointel.app.sources.SourceIds.isMarket(sourceId)) {
            return marketSourceRepo.findById(com.motointel.app.sources.SourceIds.marketId(sourceId))
                    .map(MarketSource::getName).orElse(null);
        }
        return catalogSourceRepo.findById(sourceId).map(CatalogSource::getName).orElse(null);
    }

    private static String nullable(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
