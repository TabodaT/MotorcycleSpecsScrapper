package com.motointel.app.normalize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrlNormalizerTest {

    private final UrlNormalizer n = new UrlNormalizer();

    @Test
    void lowercasesSchemeHostAndStripsTrailingSlash() {
        assertEquals("https://example.com/Listings/123",
                n.normalize("HTTPS://Example.com/Listings/123/"));
    }

    @Test
    void dropsTrackingParams() {
        assertEquals("https://olx.ro/item/abc",
                n.normalize("https://olx.ro/item/abc?utm_source=fb&utm_medium=cpc&fbclid=xyz"));
    }

    @Test
    void keepsNonTrackingParamsSorted() {
        assertEquals("https://olx.ro/item?a=1&z=9",
                n.normalize("https://olx.ro/item?z=9&a=1&utm_campaign=x"));
    }

    @Test
    void tracksDedupeAcrossTrackingVariants() {
        String a = n.normalize("https://site.com/x/1?utm_source=a");
        String b = n.normalize("https://site.com/x/1?utm_source=b");
        assertEquals(a, b);
    }
}
