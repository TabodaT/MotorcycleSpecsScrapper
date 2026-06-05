package com.motointel.app.matching;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure string-similarity primitives used by the matching engine. */
public final class TextSimilarity {

    private TextSimilarity() {}

    /** Classic Levenshtein edit distance. */
    public static int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[m];
    }

    /** Edit similarity in [0,1]: 1 - lev / maxLen. */
    public static double ratio(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 1.0;
        return 1.0 - ((double) levenshtein(a, b) / max);
    }

    /** Jaccard overlap of two token lists in [0,1]. */
    public static double jaccard(List<String> a, List<String> b) {
        if ((a == null || a.isEmpty()) && (b == null || b.isEmpty())) return 1.0;
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) return 0.0;
        Set<String> sa = new HashSet<>(a);
        Set<String> sb = new HashSet<>(b);
        Set<String> inter = new HashSet<>(sa);
        inter.retainAll(sb);
        Set<String> union = new HashSet<>(sa);
        union.addAll(sb);
        return (double) inter.size() / union.size();
    }
}
