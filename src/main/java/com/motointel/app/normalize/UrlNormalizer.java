package com.motointel.app.normalize;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Canonical URL normalization for the listing dedupe key (§3):
 * trim + lowercase scheme/host + strip trailing slash + drop tracking query params.
 */
@Component
public class UrlNormalizer {

    private static final Set<String> TRACKING_PARAMS = Set.of(
            "gclid", "fbclid", "igshid", "ref", "mc_cid", "mc_eid", "yclid", "msclkid");

    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            String path = uri.getPath() == null ? "" : uri.getPath();
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            String query = cleanQuery(uri.getQuery());

            StringBuilder sb = new StringBuilder();
            if (!scheme.isEmpty()) {
                sb.append(scheme).append("://");
            }
            sb.append(host);
            if (uri.getPort() != -1) {
                sb.append(':').append(uri.getPort());
            }
            sb.append(path);
            if (!query.isEmpty()) {
                sb.append('?').append(query);
            }
            String result = sb.toString();
            return result.isEmpty() ? trimmed.toLowerCase() : result;
        } catch (RuntimeException e) {
            // Fallback: best-effort lowercasing + trailing-slash strip.
            String lower = trimmed.toLowerCase();
            return lower.endsWith("/") ? lower.substring(0, lower.length() - 1) : lower;
        }
    }

    private String cleanQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }
        List<String> kept = new ArrayList<>();
        for (String pair : query.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            String name = pair.contains("=") ? pair.substring(0, pair.indexOf('=')) : pair;
            String lname = name.toLowerCase();
            if (lname.startsWith("utm_") || TRACKING_PARAMS.contains(lname)) {
                continue;
            }
            kept.add(pair);
        }
        kept.sort(String::compareTo);
        return String.join("&", kept);
    }

    public static List<String> splitParams(String query) {
        return query == null ? List.of() : Arrays.asList(query.split("&"));
    }
}
