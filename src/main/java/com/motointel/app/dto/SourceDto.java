package com.motointel.app.dto;

import java.time.Instant;

public record SourceDto(
        Long id,
        String name,
        String sourceType,
        String accessMethod,
        String baseUrl,
        boolean enabled,
        String complianceStatus,
        String scheduleCron,
        Instant lastRunAt,
        Instant nextRunAt) {
}
