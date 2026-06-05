package com.motointel.app.dashboard;

import com.motointel.app.domain.ListingStatus;
import com.motointel.app.domain.MatchStatus;
import com.motointel.app.dto.DashboardSummaryDto;
import com.motointel.app.jobs.JobService;
import com.motointel.app.repo.ListingModelMatchRepository;
import com.motointel.app.repo.ManufacturerRepository;
import com.motointel.app.repo.MarketListingRepository;
import com.motointel.app.repo.MotorcycleModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ManufacturerRepository manufacturerRepo;
    private final MotorcycleModelRepository modelRepo;
    private final MarketListingRepository listingRepo;
    private final ListingModelMatchRepository matchRepo;
    private final JobService jobService;

    public DashboardService(ManufacturerRepository manufacturerRepo, MotorcycleModelRepository modelRepo,
                            MarketListingRepository listingRepo, ListingModelMatchRepository matchRepo,
                            JobService jobService) {
        this.manufacturerRepo = manufacturerRepo;
        this.modelRepo = modelRepo;
        this.listingRepo = listingRepo;
        this.matchRepo = matchRepo;
        this.jobService = jobService;
    }

    public DashboardSummaryDto summary() {
        return new DashboardSummaryDto(
                manufacturerRepo.count(),
                modelRepo.count(),
                listingRepo.count(),
                listingRepo.countByStatus(ListingStatus.ACTIVE),
                listingRepo.countByStatus(ListingStatus.REMOVED),
                listingRepo.countByStatus(ListingStatus.LIKELY_SOLD),
                matchRepo.countByStatus(MatchStatus.UNMATCHED),
                matchRepo.countByStatus(MatchStatus.NEEDS_REVIEW),
                jobService.latestJobs(),
                jobService.latestErrors());
    }
}
