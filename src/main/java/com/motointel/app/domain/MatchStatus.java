package com.motointel.app.domain;

/** Match classification statuses (§7). Stored as text. */
public final class MatchStatus {
    public static final String MATCHED = "matched";
    public static final String NEEDS_REVIEW = "needs_review";
    public static final String UNMATCHED = "unmatched";
    public static final String MANUAL_MATCH = "manual_match";
    public static final String IGNORED = "ignored";
    public static final String REJECTED = "rejected";

    private MatchStatus() {}
}
