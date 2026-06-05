package com.motointel.app.dto;

import java.math.BigDecimal;

public record CandidateDto(Long modelId, String modelName, Long variantId,
                           BigDecimal confidence, String explanation) {
}
