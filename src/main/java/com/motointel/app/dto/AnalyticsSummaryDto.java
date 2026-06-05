package com.motointel.app.dto;

import java.math.BigDecimal;

public record AnalyticsSummaryDto(
        long manufacturerCount,
        long modelCount,
        long listingCount,
        long activeCount,
        long removedCount,
        long likelySoldCount,
        long matchedCount,
        long needsReviewCount,
        long unmatchedCount,
        BigDecimal avgPrice,
        BigDecimal medianPrice,
        BigDecimal minPrice,
        BigDecimal maxPrice) {
}
