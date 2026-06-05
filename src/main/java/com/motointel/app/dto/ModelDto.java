package com.motointel.app.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ModelDto(
        Long id,
        String manufacturerName,
        String name,
        String normalizedName,
        Integer productionStartYear,
        Integer productionEndYear,
        List<Variant> variants,
        List<Spec> specs,
        List<ImageDto> images,
        List<String> aliases,
        List<SourceRef> sources) {

    public record Variant(Long id, String name, Integer yearFrom, Integer yearTo) {
    }

    public record Spec(
            BigDecimal engineCapacityCc,
            BigDecimal powerKw,
            BigDecimal torqueNm,
            BigDecimal dryWeightKg,
            BigDecimal wetWeightKg,
            BigDecimal seatHeightMm,
            BigDecimal fuelCapacityL,
            BigDecimal topSpeedKmh,
            String cooling,
            String transmission,
            String finalDrive,
            Boolean abs,
            boolean isElectric) {
    }

    public record SourceRef(String url, Instant fetchedAt) {
    }
}
