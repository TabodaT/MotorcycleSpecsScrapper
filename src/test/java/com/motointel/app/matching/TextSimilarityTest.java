package com.motointel.app.matching;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextSimilarityTest {

    @Test
    void levenshtein() {
        assertEquals(0, TextSimilarity.levenshtein("abc", "abc"));
        assertEquals(1, TextSimilarity.levenshtein("abc", "abd"));
        assertEquals(3, TextSimilarity.levenshtein("", "abc"));
    }

    @Test
    void ratio() {
        assertEquals(1.0, TextSimilarity.ratio("abc", "abc"), 0.001);
        assertEquals(1.0, TextSimilarity.ratio("", ""), 0.001);
        assertTrue(TextSimilarity.ratio("gsxr6", "gsxr600") > 0.6);
        assertTrue(TextSimilarity.ratio("gsxr6", "gsxr600") < 0.85);
    }

    @Test
    void jaccard() {
        assertEquals(1.0, TextSimilarity.jaccard(List.of("a", "b"), List.of("b", "a")), 0.001);
        assertEquals(0.0, TextSimilarity.jaccard(List.of("a"), List.of("b")), 0.001);
        assertEquals(1.0 / 3.0, TextSimilarity.jaccard(List.of("a", "b"), List.of("b", "c")), 0.001);
    }
}
