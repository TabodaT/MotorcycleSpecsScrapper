package com.motointel.app.matching;

/** A scored candidate after the engine runs. */
public record ScoredCandidate(
        Long modelId,
        String modelName,
        double baseSimilarity,
        double score,
        String explanation) {
}
