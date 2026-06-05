package com.motointel.app.domain;

/** Listing lifecycle statuses (§8). Stored as text. */
public final class ListingStatus {
    public static final String ACTIVE = "active";
    public static final String MISSING_ONCE = "missing_once";
    public static final String REMOVED = "removed";
    public static final String LIKELY_SOLD = "likely_sold";
    public static final String EXPIRED = "expired";
    public static final String UNKNOWN = "unknown";
    public static final String IGNORED = "ignored";

    private ListingStatus() {}
}
