package com.motointel.app.catalog;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure (no network) discovery of the motorcyclespecs.co.za link graph, so it can be unit-tested
 * against fixture HTML. Mirrors the original two-level crawl:
 * home page -&gt; manufacturer listing pages (under {@code /bikes/}) -&gt; model detail pages
 * (under {@code /model/}), with "Next" pagination on manufacturer pages.
 */
@Component
public class CatalogLinkExtractor {

    /** A manufacturer listing page link discovered on the home page. */
    public record ManufacturerLink(String name, String url) {
    }

    /** A model detail link discovered on a manufacturer page (with best-effort production years). */
    public record DiscoveredModel(String manufacturerName, String modelName, String url,
                                  Integer startYear, Integer endYear) {
    }

    private static final Set<String> IGNORE_TEXT = Set.of(
            "home", "manufacturer", "manufacturers", "contact", "previous", "next",
            "classic bikes", "classics", "complete manufacturer list");
    private static final Set<String> IGNORE_MANUF_FILE = Set.of(
            "classics.htm", "classics.html", "custom_bikes.htm", "custom_bikes.html",
            "individual.htm", "individual.html", "racing_bikes.htm", "racing_bikes.html",
            "video_clips.htm", "video_clips.html", "index.htm", "index.html");

    private static final Pattern YEAR_RANGE = Pattern.compile("(\\d{4})\\s*[-–—]\\s*(\\d{2,4})");
    private static final Pattern YEAR_SINGLE = Pattern.compile("\\d{4}");
    private static final int MIN_YEAR = 1885;
    private static final int MAX_YEAR = 2100;

    /** Manufacturer listing pages from the home page (links under {@code /bikes/}). */
    public List<ManufacturerLink> manufacturerLinks(Document home) {
        Elements anchors = home.select("div.subMenu a[href]");
        if (anchors.isEmpty()) {
            anchors = home.select("a[href]");
        }
        Map<String, String> byUrl = new LinkedHashMap<>(); // url -> best display name
        for (Element a : anchors) {
            String url = abs(a);
            if (url == null) {
                continue;
            }
            String lower = url.toLowerCase(Locale.ROOT);
            if (!lower.contains("/bikes/") || !(lower.endsWith(".htm") || lower.endsWith(".html"))) {
                continue;
            }
            String file = fileName(lower);
            if (IGNORE_MANUF_FILE.contains(file) || file.startsWith("complete") || file.contains("video_clip")) {
                continue;
            }
            String name = cleanName(a.text());
            if (name.isBlank()) {
                name = slugToName(stripExt(file));
            }
            if (IGNORE_TEXT.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            mergeBetterName(byUrl, url, name);
        }
        List<ManufacturerLink> out = new ArrayList<>(byUrl.size());
        byUrl.forEach((url, name) -> out.add(new ManufacturerLink(name, url)));
        return out;
    }

    /** Model detail links on one manufacturer page (links under {@code /model/}). */
    public List<DiscoveredModel> modelLinks(Document manufacturerPage, String manufacturerName) {
        Map<String, Candidate> byUrl = new LinkedHashMap<>();
        for (Element a : manufacturerPage.select("a[href]")) {
            String url = abs(a);
            if (url == null || !url.toLowerCase(Locale.ROOT).contains("/model/")) {
                continue;
            }
            String anchorText = cleanName(a.text());
            boolean fromText = !anchorText.isBlank();
            String name = fromText ? anchorText : slugToName(stripExt(fileName(url.toLowerCase(Locale.ROOT))));
            if (name.isBlank() || IGNORE_TEXT.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            Integer[] years = yearsFromRow(a);
            Candidate candidate = new Candidate(
                    new DiscoveredModel(manufacturerName, name, url, years[0], years[1]), fromText);
            // Same URL can appear twice (image link with no text + text link). Prefer a real
            // anchor-text name over a URL-slug fallback; among equals, prefer the longer name.
            Candidate existing = byUrl.get(url);
            if (existing == null || candidate.betterThan(existing)) {
                byUrl.put(url, candidate);
            }
        }
        List<DiscoveredModel> out = new ArrayList<>(byUrl.size());
        for (Candidate c : byUrl.values()) {
            out.add(c.model());
        }
        return out;
    }

    private record Candidate(DiscoveredModel model, boolean fromText) {
        boolean betterThan(Candidate other) {
            if (this.fromText != other.fromText) {
                return this.fromText;
            }
            return this.model.modelName().length() > other.model.modelName().length();
        }
    }

    /** The "Next" pagination link on a manufacturer page, if present. */
    public Optional<String> nextPageUrl(Document page) {
        for (Element a : page.select("a[href]")) {
            if (a.text().trim().equalsIgnoreCase("next")) {
                String url = abs(a);
                if (url != null) {
                    return Optional.of(url);
                }
            }
        }
        return Optional.empty();
    }

    private static void mergeBetterName(Map<String, String> byUrl, String url, String name) {
        String prev = byUrl.get(url);
        if (prev == null || prev.length() < name.length()) {
            byUrl.put(url, name);
        }
    }

    /** {start, end} production years from the table row enclosing this anchor; nulls when absent. */
    private static Integer[] yearsFromRow(Element anchor) {
        Element row = anchor;
        while (row != null && !"tr".equalsIgnoreCase(row.tagName())) {
            row = row.parent();
        }
        if (row == null) {
            return new Integer[]{null, null};
        }
        String text = row.text();
        Matcher range = YEAR_RANGE.matcher(text);
        while (range.find()) {
            Integer start = plausibleYear(range.group(1));
            if (start != null) {
                Integer end = plausibleYear(expandEndYear(range.group(1), range.group(2)));
                return new Integer[]{start, end};
            }
        }
        Matcher single = YEAR_SINGLE.matcher(text);
        while (single.find()) {
            Integer y = plausibleYear(single.group());
            if (y != null) {
                return new Integer[]{y, null};
            }
        }
        return new Integer[]{null, null};
    }

    /** "2008","12" -> "2012"; "2008","2012" -> "2012". */
    private static String expandEndYear(String start, String end) {
        if (end.length() == 2 && start.length() == 4) {
            return start.substring(0, 2) + end;
        }
        return end;
    }

    private static Integer plausibleYear(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            int y = Integer.parseInt(raw.trim());
            return (y >= MIN_YEAR && y <= MAX_YEAR) ? y : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String abs(Element a) {
        String url = a.absUrl("href");
        if (url == null || url.isBlank()) {
            url = a.attr("href");
        }
        return (url == null || url.isBlank()) ? null : url;
    }

    private static String fileName(String url) {
        String u = url;
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        int h = u.indexOf('#');
        if (h >= 0) {
            u = u.substring(0, h);
        }
        int slash = u.lastIndexOf('/');
        return slash >= 0 ? u.substring(slash + 1) : u;
    }

    private static String stripExt(String file) {
        int dot = file.lastIndexOf('.');
        return dot > 0 ? file.substring(0, dot) : file;
    }

    private static String slugToName(String slug) {
        String decoded;
        try {
            decoded = URLDecoder.decode(slug, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            decoded = slug;
        }
        return decoded.replaceAll("[_\\-]+", " ").replaceAll("\\s+", " ").trim();
    }

    private static String cleanName(String text) {
        if (text == null) {
            return "";
        }
        // \s does not match U+00A0 (nbsp) in Java regex, so normalize that explicitly first.
        return text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }
}
