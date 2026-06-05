package com.motointel.app.dto;

public record ModelAnalyticsDto(Long modelId, String modelName, long activeCount, long totalObserved) {
}
