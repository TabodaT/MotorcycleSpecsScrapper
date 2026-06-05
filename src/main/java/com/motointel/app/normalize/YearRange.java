package com.motointel.app.normalize;

/** Parsed production / posting year range. Either bound may be null. */
public record YearRange(Integer start, Integer end) {
    public static YearRange empty() {
        return new YearRange(null, null);
    }
}
