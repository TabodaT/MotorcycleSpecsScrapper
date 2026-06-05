package com.motointel.app.sources;

/**
 * The /api/sources endpoint presents catalog_sources and market_sources as one list.
 * To keep numeric ids collision-free, market sources are exposed with a large offset.
 */
public final class SourceIds {

    public static final long MARKET_OFFSET = 1_000_000_000L;

    private SourceIds() {}

    public static boolean isMarket(Long unifiedId) {
        return unifiedId != null && unifiedId >= MARKET_OFFSET;
    }

    public static long unifiedMarket(long rawMarketId) {
        return MARKET_OFFSET + rawMarketId;
    }

    public static long unifiedCatalog(long rawCatalogId) {
        return rawCatalogId;
    }

    public static long marketId(long unifiedId) {
        return unifiedId - MARKET_OFFSET;
    }
}
