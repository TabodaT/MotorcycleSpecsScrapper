package com.motointel.app.domain;

/** Job type/status vocabularies. Stored as text. */
public final class JobVocab {
    // types
    public static final String TYPE_CATALOG_INGEST = "catalog_ingest";
    public static final String TYPE_MARKET_INGEST = "market_ingest";
    public static final String TYPE_CSV_IMPORT = "csv_import";
    public static final String TYPE_LIFECYCLE_SCAN = "lifecycle_scan";

    // statuses
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_COMPLETED_WITH_ERRORS = "completed_with_errors";
    public static final String STATUS_FAILED = "failed";

    private JobVocab() {}
}
