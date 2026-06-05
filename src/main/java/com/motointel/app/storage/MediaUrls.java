package com.motointel.app.storage;

/** Builds the URL the SPA should use to render a stored image / external image. */
public final class MediaUrls {

    private MediaUrls() {}

    public static String forRef(String storageRef) {
        if (storageRef == null || storageRef.isBlank()) {
            return null;
        }
        String lower = storageRef.toLowerCase();
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return storageRef; // already a usable external URL
        }
        return "/api/media/" + storageRef;
    }
}
