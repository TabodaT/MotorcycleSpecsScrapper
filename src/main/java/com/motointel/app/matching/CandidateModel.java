package com.motointel.app.matching;

import java.util.List;

/**
 * Immutable candidate fed to the matching engine.
 *
 * @param modelId        model id
 * @param modelName      display name (e.g. "Honda CBR600RR")
 * @param normalizedName the model's full alphanumeric code (e.g. "cbr600rr")
 * @param aliases        alias strings (any form; engine normalizes them)
 * @param prodStart      production start year (nullable)
 * @param prodEnd        production end year (nullable)
 * @param capacitiesCc   known variant/spec capacities in cc (for the capacity boost)
 */
public record CandidateModel(
        Long modelId,
        String modelName,
        String normalizedName,
        List<String> aliases,
        Integer prodStart,
        Integer prodEnd,
        List<Integer> capacitiesCc) {
}
