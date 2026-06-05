package com.motointel.app.dto;

import java.util.List;

public record DashboardSummaryDto(
        long manufacturerCount,
        long modelCount,
        long listingCount,
        long activeCount,
        long removedCount,
        long likelySoldCount,
        long unmatchedCount,
        long needsReviewCount,
        List<JobDto> latestJobs,
        List<JobErrorDto> latestErrors) {
}
