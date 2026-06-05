package com.motointel.app.dto;

import java.math.BigDecimal;

public record PriceStatsDto(
        Long modelId,
        String modelName,
        long count,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal avgPrice,
        BigDecimal medianPrice) {
}
