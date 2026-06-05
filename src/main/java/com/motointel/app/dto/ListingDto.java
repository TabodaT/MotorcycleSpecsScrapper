package com.motointel.app.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ListingDto(
        Long id,
        String source,
        String url,
        String title,
        String description,
        BigDecimal price,
        String currency,
        String location,
        String sellerType,
        LocalDate postedDate,
        Instant firstObservedAt,
        Instant lastObservedAt,
        String status,
        Extracted extracted,
        Match match,
        List<PriceHistory> priceHistory,
        List<StatusHistory> statusHistory,
        List<ImageDto> images,
        String snapshotRef) {

    public record Extracted(Integer year, String manufacturer, String modelText,
                            BigDecimal capacityCc, Integer mileageKm) {
    }

    public record Match(String status, Long modelId, String modelName, Long variantId,
                        BigDecimal confidence, String explanation) {
    }

    public record PriceHistory(BigDecimal price, String currency, Instant observedAt) {
    }

    public record StatusHistory(String status, Instant at) {
    }
}
