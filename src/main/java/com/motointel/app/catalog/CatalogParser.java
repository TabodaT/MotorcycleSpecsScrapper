package com.motointel.app.catalog;

import com.motointel.app.normalize.Normalizer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Defensive jsoup parser for catalog spec pages (replaces HtmlUnit + hardcoded table24).
 * It iterates ALL candidate tables and picks the one richest in 2-cell rows, so it tolerates
 * layout drift. Every numeric parse is wrapped so a bad cell yields null instead of throwing.
 */
@Component
public class CatalogParser {

    private final Normalizer normalizer;

    public CatalogParser(Normalizer normalizer) {
        this.normalizer = normalizer;
    }

    /** Extract (name, value) spec pairs from the richest 2-column table on the page. */
    public List<String[]> extractSpecPairs(Element root) {
        Elements tables = root.select("table");
        Element best = null;
        int bestCount = 0;
        for (Element table : tables) {
            int count = 0;
            for (Element row : table.select("tr")) {
                if (row.select("td").size() == 2) {
                    count++;
                }
            }
            if (count > bestCount) {
                bestCount = count;
                best = table;
            }
        }
        List<String[]> pairs = new ArrayList<>();
        if (best == null) {
            return pairs;
        }
        for (Element row : best.select("tr")) {
            Elements cells = row.select("td");
            if (cells.size() == 2) {
                String name = cells.get(0).text().trim();
                String value = cells.get(1).text().trim();
                if (!name.isEmpty()) {
                    pairs.add(new String[]{name, value});
                }
            }
        }
        return pairs;
    }

    /** Map raw spec pairs to normalized canonical values. Best-effort; never throws. */
    public SpecValues mapSpecs(List<String[]> pairs) {
        BigDecimal capacity = pick(pairs, n -> n.contains("capacity"), normalizer::parseCapacityCc);
        BigDecimal power = pick(pairs, n -> n.contains("power") && !n.contains("weight"), normalizer::parsePowerKw);
        BigDecimal torque = pick(pairs, n -> n.contains("torque"), normalizer::parseTorqueNm);
        BigDecimal dry = pick(pairs, n -> n.contains("dry"), normalizer::parseWeightKg);
        BigDecimal wet = pick(pairs, n -> n.contains("wet") || n.contains("kerb") || n.contains("curb"),
                normalizer::parseWeightKg);
        if (dry == null && wet == null) {
            dry = pick(pairs, n -> n.contains("weight"), normalizer::parseWeightKg);
        }
        BigDecimal seat = pick(pairs, n -> n.contains("seat"), normalizer::parseLengthMm);
        BigDecimal fuel = pick(pairs, n -> n.contains("fuel") && n.contains("capacity"), normalizer::parseDecimal);
        BigDecimal topSpeed = pick(pairs, n -> n.contains("top speed")
                || (n.contains("speed") && !n.contains("idle")), normalizer::parseSpeedKmh);

        String cooling = text(pairs, n -> n.contains("cool"));
        String transmission = text(pairs, n -> n.contains("transmission") || n.contains("gearbox") || n.contains("gear"));
        String finalDrive = text(pairs, n -> n.contains("final drive") || n.contains("drive"));
        Boolean abs = absFlag(pairs);
        boolean electric = pairs.stream().anyMatch(p ->
                normalizer.isElectric(p[0]) || normalizer.isElectric(p[1]));

        return new SpecValues(capacity, power, torque, dry, wet, seat, fuel, topSpeed,
                cooling, transmission, finalDrive, abs, electric);
    }

    private BigDecimal pick(List<String[]> pairs, java.util.function.Predicate<String> nameMatch,
                            Function<String, Optional<BigDecimal>> conv) {
        for (String[] p : pairs) {
            String name = p[0].toLowerCase();
            if (nameMatch.test(name)) {
                try {
                    Optional<BigDecimal> v = conv.apply(p[1]);
                    if (v.isPresent()) {
                        return v.get();
                    }
                } catch (RuntimeException ignored) {
                    // defensive: skip unparseable cell, keep scanning
                }
            }
        }
        return null;
    }

    private String text(List<String[]> pairs, java.util.function.Predicate<String> nameMatch) {
        for (String[] p : pairs) {
            if (nameMatch.test(p[0].toLowerCase()) && !p[1].isBlank()) {
                return p[1];
            }
        }
        return null;
    }

    private Boolean absFlag(List<String[]> pairs) {
        for (String[] p : pairs) {
            String name = p[0].toLowerCase();
            String value = p[1].toLowerCase();
            if (name.contains("abs") || value.contains("abs")) {
                return !value.contains("no abs") && !value.contains("non-abs");
            }
            if (name.contains("brake") && value.contains("abs")) {
                return true;
            }
        }
        return null;
    }

    /** Convenience for a full Document. */
    public SpecValues parse(Document doc) {
        return mapSpecs(extractSpecPairs(doc));
    }
}
