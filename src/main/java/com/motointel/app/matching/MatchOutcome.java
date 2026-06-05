package com.motointel.app.matching;

import java.util.List;

/**
 * Result of running the matching engine.
 *
 * @param status     one of matched / needs_review / unmatched
 * @param top        winning candidate (nullable when no candidates)
 * @param topScore   final score of the top candidate
 * @param secondScore final score of the runner-up (0 if none)
 * @param explanation human-readable explanation for the chosen status
 * @param candidates ranked candidates (top 5), each with its own explanation
 */
public record MatchOutcome(
        String status,
        ScoredCandidate top,
        double topScore,
        double secondScore,
        String explanation,
        List<ScoredCandidate> candidates) {
}
