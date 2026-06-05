package com.motointel.app.imports;

import java.util.List;
import java.util.Set;

/**
 * The single source of truth for the market-listing CSV contract (§3).
 * The import template, the validator, the parser, and the demo seed CSV all derive
 * from {@link #HEADER} — they MUST be byte-identical on the header row.
 */
public final class CsvContract {

    public static final List<String> COLUMNS = List.of(
            "source", "url", "title", "price", "currency", "observed_at",
            "description", "location", "posted_date", "seller_type",
            "external_id", "mileage_km", "image_url");

    public static final Set<String> REQUIRED = Set.of(
            "source", "url", "title", "price", "currency", "observed_at");

    /** The canonical header row (no trailing newline). */
    public static final String HEADER = String.join(",", COLUMNS);

    private CsvContract() {}
}
