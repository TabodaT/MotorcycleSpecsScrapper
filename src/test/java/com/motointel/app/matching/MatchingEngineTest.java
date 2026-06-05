package com.motointel.app.matching;

import com.motointel.app.domain.MatchStatus;
import com.motointel.app.normalize.Normalizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchingEngineTest {

    private final MatchingEngine engine = new MatchingEngine(new Normalizer());

    private final CandidateModel cbr600rr = new CandidateModel(1L, "Honda CBR600RR", "cbr600rr",
            List.of("CBR 600 RR", "CBR600RR", "Honda CBR600RR"), 2003, 2022, List.of(599));
    private final CandidateModel gsxr600 = new CandidateModel(2L, "Suzuki GSX-R600", "gsxr600",
            List.of("GSXR 600", "GSX R 600"), 2001, 2024, List.of(599));
    private final CandidateModel yzfr6 = new CandidateModel(3L, "Yamaha YZF-R6", "yzfr6",
            List.of("R6", "YZF R6"), 1999, 2020, List.of(599));
    private final CandidateModel mt07 = new CandidateModel(4L, "Yamaha MT-07", "mt07",
            List.of("MT 07", "MT07"), 2014, 2024, List.of(689));
    private final CandidateModel transalp = new CandidateModel(5L, "Honda Transalp", "transalp",
            List.of("Trans Alp"), 2023, 2024, List.of(755));

    private final List<CandidateModel> all = List.of(cbr600rr, gsxr600, yzfr6, mt07, transalp);

    private MatchOutcome match(String text) {
        return engine.match(new MatchInput(text, null, null), all);
    }

    @Test
    void cbr600rrVariants() {
        MatchOutcome a = match("CBR 600 RR");
        assertEquals(MatchStatus.MATCHED, a.status());
        assertEquals(1L, a.top().modelId());

        MatchOutcome b = match("CBR600RR");
        assertEquals(MatchStatus.MATCHED, b.status());
        assertEquals(1L, b.top().modelId());
    }

    @Test
    void gsxr600Variants() {
        assertEquals(2L, match("GSXR 600").top().modelId());
        assertEquals(MatchStatus.MATCHED, match("GSXR 600").status());
        assertEquals(2L, match("GSX R 600").top().modelId());
        assertEquals(MatchStatus.MATCHED, match("GSX R 600").status());
    }

    @Test
    void yamahaR6() {
        assertEquals(3L, match("R6").top().modelId());
        assertEquals(3L, match("YZF R6").top().modelId());
    }

    @Test
    void mt07() {
        assertEquals(4L, match("MT 07").top().modelId());
        assertEquals(MatchStatus.MATCHED, match("MT 07").status());
        assertEquals(4L, match("MT07").top().modelId());
        assertEquals(MatchStatus.MATCHED, match("MT07").status());
    }

    @Test
    void transalp() {
        assertEquals(5L, match("Trans Alp").top().modelId());
        assertEquals(MatchStatus.MATCHED, match("Trans Alp").status());
    }

    @Test
    void realisticAdMatchesByCode() {
        MatchOutcome o = engine.match(new MatchInput("Honda CBR 600 RR 2008 low mileage", 2008, 599), all);
        assertEquals(MatchStatus.MATCHED, o.status());
        assertEquals(1L, o.top().modelId());
    }

    @Test
    void junkListingIsUnmatched() {
        MatchOutcome o = match("Refrigerator for sale, white, like new");
        assertEquals(MatchStatus.UNMATCHED, o.status());
    }

    @Test
    void ambiguousNearMarginNeedsReview() {
        // Two near-identical codes: top is exact (1.0), runner-up ~0.875 -> margin < 0.15 -> needs_review.
        CandidateModel a = new CandidateModel(10L, "Model A", "cbr600rr", List.of(), null, null, List.of());
        CandidateModel b = new CandidateModel(11L, "Model B", "cbr600rs", List.of(), null, null, List.of());
        MatchOutcome o = engine.match(new MatchInput("CBR600RR", null, null), List.of(a, b));
        assertEquals(MatchStatus.NEEDS_REVIEW, o.status());
        assertEquals(10L, o.top().modelId());
    }

    @Test
    void persistsTopFiveCandidatesWithExplanations() {
        MatchOutcome o = match("CBR 600 RR");
        assertEquals(5, o.candidates().size());
        o.candidates().forEach(c -> org.junit.jupiter.api.Assertions.assertNotNull(c.explanation()));
    }
}
