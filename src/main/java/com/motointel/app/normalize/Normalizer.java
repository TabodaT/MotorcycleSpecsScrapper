package com.motointel.app.normalize;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure, side-effect-free normalization + unit conversion utilities. Ported (not copied)
 * from the legacy MotoModelsMapper, with the hp&rarr;kW bug fixed (multiply by 0.7457).
 *
 * <p>Conversion factors:
 * <ul>
 *   <li>hp &rarr; kW: &times; 0.7457 (mechanical horsepower)</li>
 *   <li>PS/CV &rarr; kW: &times; 0.7355 (metric horsepower)</li>
 *   <li>kgf&middot;m &rarr; Nm: &times; 9.80665</li>
 *   <li>ft&middot;lb &rarr; Nm: &times; 1.3558</li>
 *   <li>in &rarr; mm: &times; 25.4</li>
 *   <li>lb &rarr; kg: &times; 0.453592</li>
 *   <li>mph &rarr; km/h: &times; 1.609</li>
 *   <li>mpg &rarr; l/100km: 235.21 / mpg</li>
 * </ul>
 */
@Component
public class Normalizer {

    public static final double HP_TO_KW = 0.7457;
    public static final double PS_TO_KW = 0.7355;
    public static final double KGM_TO_NM = 9.80665;
    public static final double FTLB_TO_NM = 1.3558;
    public static final double IN_TO_MM = 25.4;
    public static final double LB_TO_KG = 0.453592;
    public static final double MPH_TO_KMH = 1.609;
    public static final double MPG_DIVIDEND = 235.21;

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d+)?");
    private static final Pattern YEAR = Pattern.compile("(19|20)\\d{2}");

    // ---------------------------------------------------------------------
    // Text normalization (drives matching)
    // ---------------------------------------------------------------------

    /** Lowercase, replace any non-alphanumeric run with a single space, trim. */
    public String normalizeText(String raw) {
        if (raw == null) {
            return "";
        }
        String lowered = raw.toLowerCase().trim();
        return NON_ALNUM.matcher(lowered).replaceAll(" ").trim();
    }

    /** Normalized text with spaces removed — the compact alphanumeric form (e.g. "cbr600rr"). */
    public String compact(String raw) {
        return normalizeText(raw).replace(" ", "");
    }

    /** Normalized tokens (non-empty). */
    public List<String> tokens(String raw) {
        String n = normalizeText(raw);
        if (n.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(n.split(" "));
    }

    // ---------------------------------------------------------------------
    // Pure numeric conversions
    // ---------------------------------------------------------------------

    public BigDecimal hpToKw(double hp) { return round(hp * HP_TO_KW); }
    public BigDecimal psToKw(double ps) { return round(ps * PS_TO_KW); }
    public BigDecimal kgmToNm(double kgm) { return round(kgm * KGM_TO_NM); }
    public BigDecimal ftLbToNm(double ftlb) { return round(ftlb * FTLB_TO_NM); }
    public BigDecimal inchToMm(double inch) { return round(inch * IN_TO_MM); }
    public BigDecimal lbToKg(double lb) { return round(lb * LB_TO_KG); }
    public BigDecimal mphToKmh(double mph) { return round(mph * MPH_TO_KMH); }

    public BigDecimal mpgToL100km(double mpg) {
        if (mpg <= 0) {
            return null;
        }
        return round(MPG_DIVIDEND / mpg);
    }

    // ---------------------------------------------------------------------
    // Raw spec-string parsers (detect unit, convert to canonical metric)
    // ---------------------------------------------------------------------

    /** Power -&gt; kW. Recognises kw, hp/bhp (&times;0.7457), ps/cv (&times;0.7355). */
    public Optional<BigDecimal> parsePowerKw(String raw) {
        String s = lower(raw);
        Optional<Double> n = firstNumber(s);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        double v = n.get();
        if (s.contains("kw")) {
            return Optional.of(round(v));
        }
        if (s.contains("bhp") || s.contains("hp")) {
            return Optional.of(hpToKw(v));
        }
        if (s.contains("ps") || s.contains("cv") || s.contains("ch")) {
            return Optional.of(psToKw(v));
        }
        // No unit hint — assume the value is already kW.
        return Optional.of(round(v));
    }

    /** Torque -&gt; Nm. Recognises nm, kgm/kgf-m (&times;9.80665), ft-lb/lb-ft (&times;1.3558). */
    public Optional<BigDecimal> parseTorqueNm(String raw) {
        String s = lower(raw);
        Optional<Double> n = firstNumber(s);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        double v = n.get();
        if (s.contains("nm") || s.contains("n.m") || s.contains("n-m")) {
            return Optional.of(round(v));
        }
        if (s.contains("kg")) {
            return Optional.of(kgmToNm(v));
        }
        if (s.contains("ft") || s.contains("lb")) {
            return Optional.of(ftLbToNm(v));
        }
        return Optional.of(round(v));
    }

    /** Length -&gt; mm. Recognises mm, cm (&times;10), in/&quot; (&times;25.4). */
    public Optional<BigDecimal> parseLengthMm(String raw) {
        String s = lower(raw);
        Optional<Double> n = firstNumber(s);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        double v = n.get();
        if (s.contains("mm")) {
            return Optional.of(round(v));
        }
        if (s.contains("cm")) {
            return Optional.of(round(v * 10));
        }
        if (s.contains("in") || s.contains("\"")) {
            return Optional.of(inchToMm(v));
        }
        return Optional.of(round(v));
    }

    /** Weight -&gt; kg. Recognises kg, lb/lbs/pounds (&times;0.453592). */
    public Optional<BigDecimal> parseWeightKg(String raw) {
        String s = lower(raw);
        Optional<Double> n = firstNumber(s);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        double v = n.get();
        if (s.contains("kg")) {
            return Optional.of(round(v));
        }
        if (s.contains("lb") || s.contains("pound")) {
            return Optional.of(lbToKg(v));
        }
        return Optional.of(round(v));
    }

    /** Speed -&gt; km/h. Recognises km/h variants, mph (&times;1.609). */
    public Optional<BigDecimal> parseSpeedKmh(String raw) {
        String s = lower(raw);
        Optional<Double> n = firstNumber(s);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        double v = n.get();
        if (s.contains("mph")) {
            return Optional.of(mphToKmh(v));
        }
        // km/h, kmh, kph or unitless -> already km/h
        return Optional.of(round(v));
    }

    /** Consumption -&gt; l/100km. Recognises l/100km and mpg (235.21 / mpg). */
    public Optional<BigDecimal> parseConsumptionL100km(String raw) {
        String s = lower(raw);
        Optional<Double> n = firstNumber(s);
        if (n.isEmpty()) {
            return Optional.empty();
        }
        double v = n.get();
        if (s.contains("mpg") || (s.contains("mp") && !s.contains("100"))) {
            return Optional.ofNullable(mpgToL100km(v));
        }
        return Optional.of(round(v));
    }

    /** Engine capacity -&gt; cc. Recognises cc/ccm directly, otherwise first number. */
    public Optional<BigDecimal> parseCapacityCc(String raw) {
        String s = lower(raw);
        return firstNumber(s).map(this::round);
    }

    /** First decimal number found in the text (unit-agnostic), e.g. for fuel litres. */
    public Optional<BigDecimal> parseDecimal(String raw) {
        return firstNumber(lower(raw)).map(this::round);
    }

    // ---------------------------------------------------------------------
    // Extraction heuristics
    // ---------------------------------------------------------------------

    /**
     * Capacity inferred from a model name, e.g. "CBR600RR" -&gt; 600, "GSX-R 750" -&gt; 750.
     * Only returns a value when a number &ge; 99 is present (avoids model codes like R6 -&gt; 6).
     */
    public Optional<Integer> capacityFromModelName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        Matcher m = NUMBER.matcher(name.toLowerCase());
        Integer best = null;
        while (m.find()) {
            String g = m.group().replace(",", ".");
            int val = (int) Math.round(Double.parseDouble(g));
            if (val >= 99 && val <= 3000) {
                // Prefer the largest plausible cc figure found.
                if (best == null || val > best) {
                    best = val;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /** True when text suggests an electric motorcycle. */
    public boolean isElectric(String text) {
        String s = lower(text);
        return s.contains("electric") || s.contains("kwh") || s.contains("charging")
                || s.contains(" charg") || s.matches(".*\\be-?bike\\b.*");
    }

    /**
     * Parse a production / posting year range such as "2003 - 2006", "2010-", "1998".
     * A 2-digit end year is expanded relative to the start ("2003-06" -&gt; 2006).
     */
    public YearRange parseYearRange(String raw) {
        if (raw == null || raw.isBlank()) {
            return YearRange.empty();
        }
        List<Integer> fullYears = new ArrayList<>();
        Matcher ym = YEAR.matcher(raw);
        while (ym.find()) {
            fullYears.add(Integer.parseInt(ym.group()));
        }
        if (fullYears.size() >= 2) {
            return new YearRange(fullYears.get(0), fullYears.get(1));
        }
        if (fullYears.size() == 1) {
            int start = fullYears.get(0);
            // look for a trailing 2-digit end (e.g. "2003-06")
            Matcher two = Pattern.compile("\\b(19|20)\\d{2}\\s*[-/]\\s*(\\d{2})\\b").matcher(raw);
            if (two.find()) {
                int end2 = Integer.parseInt(two.group(2));
                int century = (start / 100) * 100;
                int end = century + end2;
                if (end < start) {
                    end += 100;
                }
                return new YearRange(start, end);
            }
            return new YearRange(start, start);
        }
        return YearRange.empty();
    }

    /** Single year extracted from text (first 4-digit 19xx/20xx), if any. */
    public Optional<Integer> extractYear(String text) {
        if (text == null) {
            return Optional.empty();
        }
        Matcher m = YEAR.matcher(text);
        return m.find() ? Optional.of(Integer.parseInt(m.group())) : Optional.empty();
    }

    /** Mileage in km extracted from text, tolerating thousands separators. */
    public Optional<Integer> extractMileageKm(String text) {
        if (text == null) {
            return Optional.empty();
        }
        String s = text.toLowerCase();
        Matcher m = Pattern.compile("(\\d[\\d.,\\s]{0,9}\\d|\\d)\\s*(km|kms|kilometers?|kilometres?|miles?|mi)\\b")
                .matcher(s);
        if (m.find()) {
            String digits = m.group(1).replaceAll("[.,\\s]", "");
            try {
                int val = Integer.parseInt(digits);
                if (m.group(2).startsWith("mi")) {
                    val = (int) Math.round(val * MPH_TO_KMH);
                }
                return Optional.of(val);
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /** Detect a known manufacturer name appearing in free text. */
    public Optional<String> detectManufacturer(String text, List<String> knownNormalizedNames) {
        String n = normalizeText(text);
        for (String manu : knownNormalizedNames) {
            if (n.equals(manu) || n.contains(" " + manu + " ")
                    || n.startsWith(manu + " ") || n.endsWith(" " + manu) || n.contains(manu)) {
                return Optional.of(manu);
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private String lower(String raw) {
        return raw == null ? "" : raw.toLowerCase();
    }

    private Optional<Double> firstNumber(String s) {
        Matcher m = NUMBER.matcher(s);
        if (m.find()) {
            return Optional.of(Double.parseDouble(m.group().replace(",", ".")));
        }
        return Optional.empty();
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
