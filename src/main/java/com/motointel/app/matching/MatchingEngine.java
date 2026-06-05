package com.motointel.app.matching;

import com.motointel.app.domain.MatchStatus;
import com.motointel.app.normalize.Normalizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Deterministic listing&rarr;model matcher implementing the pinned scoring of §7.
 *
 * <pre>
 * base similarity (model code + aliases) -> gated boosts (base >= 0.50)
 *   +0.10 year in [start,end]; +0.10 capacity within +/-5% of a known capacity
 *   -0.15 ambiguity penalty if >= 2 candidates (base >= 0.50) within 0.10 of top
 * classify (first match wins):
 *   1. unmatched   if top < 0.55
 *   2. matched     if top >= 0.85 AND (top - second) >= 0.15
 *   3. needs_review otherwise
 * </pre>
 */
@Component
public class MatchingEngine {

    public static final double BASE_GATE = 0.50;
    public static final double BOOST_YEAR = 0.10;
    public static final double BOOST_CAPACITY = 0.10;
    public static final double AMBIGUITY_PENALTY = 0.15;
    public static final double AMBIGUITY_WINDOW = 0.10;
    public static final double UNMATCHED_BELOW = 0.55;
    public static final double MATCHED_AT_LEAST = 0.85;
    public static final double MATCHED_MARGIN = 0.15;
    public static final double CAPACITY_TOLERANCE = 0.05;

    private final Normalizer normalizer;

    public MatchingEngine(Normalizer normalizer) {
        this.normalizer = normalizer;
    }

    public MatchOutcome match(MatchInput input, List<CandidateModel> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return new MatchOutcome(MatchStatus.UNMATCHED, null, 0, 0,
                    "No catalog models available to match against.", List.of());
        }

        String listingText = input.listingText() == null ? "" : input.listingText();
        List<Scored> scored = new ArrayList<>();
        for (CandidateModel c : candidates) {
            double base = baseSimilarity(listingText, c);
            double score = base;
            List<String> boosts = new ArrayList<>();
            if (base >= BASE_GATE) {
                if (yearInRange(input.year(), c)) {
                    score += BOOST_YEAR;
                    boosts.add("year");
                }
                if (capacityMatches(input.capacityCc(), c)) {
                    score += BOOST_CAPACITY;
                    boosts.add("capacity");
                }
            }
            score = clamp(score);
            scored.add(new Scored(c, base, score, boosts));
        }

        scored.sort(Comparator.comparingDouble((Scored s) -> s.score).reversed());

        // Ambiguity penalty: >= 2 candidates (base >= gate) within window of the top score.
        double preTop = scored.get(0).score;
        long ambiguousNear = scored.stream()
                .filter(s -> s.base >= BASE_GATE)
                .filter(s -> Math.abs(preTop - s.score) <= AMBIGUITY_WINDOW)
                .count();
        boolean ambiguityApplied = false;
        if (ambiguousNear >= 2) {
            Scored top = scored.get(0);
            top.score = clamp(top.score - AMBIGUITY_PENALTY);
            ambiguityApplied = true;
            scored.sort(Comparator.comparingDouble((Scored s) -> s.score).reversed());
        }

        double top = scored.get(0).score;
        double second = scored.size() > 1 ? scored.get(1).score : 0.0;

        String status;
        if (top < UNMATCHED_BELOW) {
            status = MatchStatus.UNMATCHED;
        } else if (top >= MATCHED_AT_LEAST && (top - second) >= MATCHED_MARGIN) {
            status = MatchStatus.MATCHED;
        } else {
            status = MatchStatus.NEEDS_REVIEW;
        }

        List<ScoredCandidate> ranked = new ArrayList<>();
        for (int i = 0; i < scored.size() && i < 5; i++) {
            Scored s = scored.get(i);
            ranked.add(new ScoredCandidate(s.candidate.modelId(), s.candidate.modelName(),
                    round(s.base), round(s.score), candidateExplanation(s)));
        }

        ScoredCandidate topDto = ranked.isEmpty() ? null : ranked.get(0);
        String explanation = outcomeExplanation(status, scored.get(0), second, ambiguityApplied);
        return new MatchOutcome(status, topDto, round(top), round(second), explanation, ranked);
    }

    // ---------------------------------------------------------------------
    // Base similarity (the separating signal): model code + aliases
    // ---------------------------------------------------------------------
    private double baseSimilarity(String listingText, CandidateModel c) {
        double best = similarity(listingText, c.normalizedName());
        if (c.aliases() != null) {
            for (String alias : c.aliases()) {
                best = Math.max(best, similarity(listingText, alias));
            }
        }
        return best;
    }

    /** Symmetric-ish similarity combining compact containment, edit ratio, and token overlap. */
    double similarity(String listing, String target) {
        String lc = normalizer.compact(listing);
        String tc = normalizer.compact(target);
        if (tc.isEmpty()) {
            return 0.0;
        }
        if (lc.equals(tc)) {
            return 1.0;
        }
        double containment = 0.0;
        if (tc.length() >= 3 && lc.contains(tc)) {
            containment = 1.0;
        } else if (lc.length() >= 3 && tc.contains(lc)) {
            containment = 0.85;
        }
        double edit = TextSimilarity.ratio(lc, tc);
        double token = TextSimilarity.jaccard(normalizer.tokens(listing), normalizer.tokens(target));
        return clamp(Math.max(containment, Math.max(edit, token)));
    }

    private boolean yearInRange(Integer year, CandidateModel c) {
        if (year == null || c.prodStart() == null) {
            return false;
        }
        int end = c.prodEnd() != null ? c.prodEnd() : Integer.MAX_VALUE;
        return year >= c.prodStart() && year <= end;
    }

    private boolean capacityMatches(Integer capacityCc, CandidateModel c) {
        if (capacityCc == null || c.capacitiesCc() == null) {
            return false;
        }
        for (Integer known : c.capacitiesCc()) {
            if (known != null && known > 0) {
                double tol = known * CAPACITY_TOLERANCE;
                if (Math.abs(capacityCc - known) <= tol) {
                    return true;
                }
            }
        }
        return false;
    }

    private String candidateExplanation(Scored s) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "base %.2f", s.base));
        if (!s.boosts.isEmpty()) {
            sb.append(", +").append(String.join("/+", s.boosts)).append(" boost");
        }
        sb.append(String.format(Locale.ROOT, " -> score %.2f", s.score));
        return sb.toString();
    }

    private String outcomeExplanation(String status, Scored top, double second, boolean ambiguity) {
        StringBuilder sb = new StringBuilder();
        sb.append(switch (status) {
            case MatchStatus.MATCHED -> "Auto-matched";
            case MatchStatus.UNMATCHED -> "Unmatched";
            default -> "Needs review";
        });
        sb.append(": '").append(top.candidate.normalizedName()).append("' ");
        sb.append(String.format(Locale.ROOT, "score %.2f (base %.2f), margin %.2f over next.",
                top.score, top.base, top.score - second));
        if (ambiguity) {
            sb.append(" Ambiguity penalty applied (close rivals).");
        }
        return sb.toString();
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Mutable internal scoring holder. */
    private static final class Scored {
        final CandidateModel candidate;
        final double base;
        double score;
        final List<String> boosts;

        Scored(CandidateModel candidate, double base, double score, List<String> boosts) {
            this.candidate = candidate;
            this.base = base;
            this.score = score;
            this.boosts = boosts;
        }
    }
}
