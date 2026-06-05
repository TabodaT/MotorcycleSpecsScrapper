package com.motointel.app.catalog;

import java.math.BigDecimal;

/** Normalized spec values extracted from a catalog page (all canonical metric). */
public record SpecValues(
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
