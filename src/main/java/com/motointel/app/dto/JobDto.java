package com.motointel.app.dto;

import java.time.Instant;
import java.util.List;

public record JobDto(
        Long id,
        String type,
        String source,
        String status,
        Instant startedAt,
        Instant finishedAt,
        Counts counts,
        String errorSummary,
        List<JobErrorDto> errors) {

    public record Counts(int discovered, int fetched, int parsed, int inserted, int updated, int failed) {
    }
}
