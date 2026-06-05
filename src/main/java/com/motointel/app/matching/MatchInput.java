package com.motointel.app.matching;

/**
 * Listing signal fed to the matching engine.
 *
 * @param listingText combined matchable text (title + extracted model text)
 * @param year        extracted listing year (nullable)
 * @param capacityCc  extracted capacity in cc (nullable)
 */
public record MatchInput(String listingText, Integer year, Integer capacityCc) {
}
