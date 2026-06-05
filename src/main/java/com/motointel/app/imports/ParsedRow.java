package com.motointel.app.imports;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** A validated CSV row ready for upsert. */
public record ParsedRow(
        int rowNumber,
        String source,
        String url,
        String normalizedUrl,
        String title,
        BigDecimal price,
        String currency,
        Instant observedAt,
        String description,
        String location,
        LocalDate postedDate,
        String sellerType,
        String externalId,
        Integer mileageKm,
        String imageUrl) {

    /** Dedupe key per §3: (source, external_id) when present, else (source, normalized_url). */
    public String dedupeKey() {
        return externalId != null && !externalId.isBlank()
                ? "id:" + source + "|" + externalId
                : "url:" + source + "|" + normalizedUrl;
    }
}
