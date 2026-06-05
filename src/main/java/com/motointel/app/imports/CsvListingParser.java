package com.motointel.app.imports;

import com.motointel.app.dto.ImportResultDto;
import com.motointel.app.normalize.UrlNormalizer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure CSV parser + validator for the §3 contract. No persistence — fully unit-testable.
 * Required fields: source, url, title, price, currency, observed_at. Others optional.
 */
@Component
public class CsvListingParser {

    private static final DateTimeFormatter SPACE_DATETIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UrlNormalizer urlNormalizer;

    public CsvListingParser(UrlNormalizer urlNormalizer) {
        this.urlNormalizer = urlNormalizer;
    }

    public CsvParseResult parse(String content) {
        List<ParsedRow> rows = new ArrayList<>();
        List<ImportResultDto.RowError> errors = new ArrayList<>();

        if (content == null || content.isBlank()) {
            errors.add(new ImportResultDto.RowError(0, "file", "CSV is empty"));
            return new CsvParseResult(rows, errors);
        }
        String clean = stripBom(content);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (CSVParser parser = CSVParser.parse(new StringReader(clean), format)) {
            // Header validation: required columns must be present.
            List<String> header = parser.getHeaderNames();
            for (String required : CsvContract.REQUIRED) {
                if (!header.contains(required)) {
                    errors.add(new ImportResultDto.RowError(0, required, "Missing required column"));
                }
            }
            if (!errors.isEmpty()) {
                return new CsvParseResult(rows, errors);
            }

            for (CSVRecord record : parser) {
                int rowNum = (int) record.getRecordNumber();
                List<ImportResultDto.RowError> rowErrors = new ArrayList<>();

                String source = get(record, "source");
                String url = get(record, "url");
                String title = get(record, "title");
                String priceRaw = get(record, "price");
                String currency = get(record, "currency");
                String observedRaw = get(record, "observed_at");

                requireField(rowNum, "source", source, rowErrors);
                requireField(rowNum, "url", url, rowErrors);
                requireField(rowNum, "title", title, rowErrors);
                requireField(rowNum, "price", priceRaw, rowErrors);
                requireField(rowNum, "currency", currency, rowErrors);
                requireField(rowNum, "observed_at", observedRaw, rowErrors);

                BigDecimal price = null;
                if (notBlank(priceRaw)) {
                    try {
                        price = new BigDecimal(priceRaw.replace(",", "").trim());
                        if (price.signum() < 0) {
                            rowErrors.add(new ImportResultDto.RowError(rowNum, "price", "must be non-negative"));
                            price = null;
                        }
                    } catch (NumberFormatException e) {
                        rowErrors.add(new ImportResultDto.RowError(rowNum, "price", "not a valid decimal"));
                    }
                }
                if (notBlank(currency) && !currency.matches("[A-Za-z]{3}")) {
                    rowErrors.add(new ImportResultDto.RowError(rowNum, "currency", "must be a 3-letter ISO code"));
                }
                Instant observedAt = null;
                if (notBlank(observedRaw)) {
                    observedAt = parseInstant(observedRaw);
                    if (observedAt == null) {
                        rowErrors.add(new ImportResultDto.RowError(rowNum, "observed_at", "not ISO-8601 date/datetime"));
                    }
                }

                LocalDate postedDate = null;
                String postedRaw = get(record, "posted_date");
                if (notBlank(postedRaw)) {
                    postedDate = parseDate(postedRaw);
                    if (postedDate == null) {
                        rowErrors.add(new ImportResultDto.RowError(rowNum, "posted_date", "not an ISO-8601 date"));
                    }
                }

                Integer mileageKm = null;
                String mileageRaw = get(record, "mileage_km");
                if (notBlank(mileageRaw)) {
                    try {
                        int v = Integer.parseInt(mileageRaw.replace(",", "").replace(" ", "").trim());
                        if (v < 0) {
                            rowErrors.add(new ImportResultDto.RowError(rowNum, "mileage_km", "must be >= 0"));
                        } else {
                            mileageKm = v;
                        }
                    } catch (NumberFormatException e) {
                        rowErrors.add(new ImportResultDto.RowError(rowNum, "mileage_km", "must be an integer"));
                    }
                }

                if (!rowErrors.isEmpty()) {
                    errors.addAll(rowErrors);
                    continue;
                }

                String normalizedUrl = urlNormalizer.normalize(url);
                rows.add(new ParsedRow(rowNum, source.trim(), url.trim(), normalizedUrl, title.trim(),
                        price, currency.toUpperCase().trim(), observedAt,
                        emptyToNull(get(record, "description")), emptyToNull(get(record, "location")),
                        postedDate, emptyToNull(get(record, "seller_type")),
                        emptyToNull(get(record, "external_id")), mileageKm,
                        emptyToNull(get(record, "image_url"))));
            }
        } catch (Exception e) {
            errors.add(new ImportResultDto.RowError(0, "file", "Could not parse CSV: " + e.getMessage()));
        }

        return new CsvParseResult(rows, errors);
    }

    private static void requireField(int row, String field, String value, List<ImportResultDto.RowError> errs) {
        if (!notBlank(value)) {
            errs.add(new ImportResultDto.RowError(row, field, "is required"));
        }
    }

    private static String get(CSVRecord record, String column) {
        return record.isMapped(column) ? record.get(column) : null;
    }

    static Instant parseInstant(String raw) {
        String s = raw.trim();
        try {
            return Instant.parse(s);
        } catch (Exception ignored) { /* try next */ }
        try {
            return OffsetDateTime.parse(s).toInstant();
        } catch (Exception ignored) { /* try next */ }
        try {
            return LocalDateTime.parse(s).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) { /* try next */ }
        try {
            return LocalDateTime.parse(s, SPACE_DATETIME).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) { /* try next */ }
        LocalDate d = parseDate(s);
        return d == null ? null : d.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    static LocalDate parseDate(String raw) {
        String s = raw.trim();
        try {
            return LocalDate.parse(s);
        } catch (Exception ignored) { /* not a plain date */ }
        try {
            return OffsetDateTime.parse(s).toLocalDate();
        } catch (Exception ignored) { /* not an offset datetime */ }
        try {
            return LocalDateTime.parse(s).toLocalDate();
        } catch (Exception ignored) { /* not a local datetime */ }
        try {
            return LocalDateTime.parse(s, SPACE_DATETIME).toLocalDate();
        } catch (Exception ignored) { /* give up */ }
        return null;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String emptyToNull(String s) {
        return notBlank(s) ? s.trim() : null;
    }

    private static String stripBom(String s) {
        return (!s.isEmpty() && s.charAt(0) == '﻿') ? s.substring(1) : s;
    }
}
