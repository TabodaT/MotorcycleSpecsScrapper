package com.motointel.app.dto;

import java.time.Instant;

public record JobErrorDto(Long id, String url, String stage, String message, String detail, Instant createdAt) {
}
