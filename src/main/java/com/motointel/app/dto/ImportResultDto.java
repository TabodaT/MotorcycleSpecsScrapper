package com.motointel.app.dto;

import java.util.List;

public record ImportResultDto(Long jobId, int inserted, int updated, int failed, List<RowError> rowErrors) {

    public record RowError(int row, String field, String message) {
    }
}
