package com.motointel.app.imports;

import com.motointel.app.normalize.UrlNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvListingParserTest {

    private final CsvListingParser parser = new CsvListingParser(new UrlNormalizer());

    @Test
    void canonicalHeaderIsStable() {
        assertEquals("source,url,title,price,currency,observed_at,description,location,"
                + "posted_date,seller_type,external_id,mileage_km,image_url", CsvContract.HEADER);
    }

    @Test
    void parsesValidRows() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/1,Honda CBR600RR,6500,EUR,2024-05-01,desc,Cluj,2024-04-28,private,a1,20000,\n"
                + "OLX,https://x.test/olx/2,Yamaha MT-07,7000,EUR,2024-05-02,,Iasi,,dealer,a2,,\n";
        CsvParseResult r = parser.parse(csv);
        assertTrue(r.errors().isEmpty(), "no errors expected: " + r.errors());
        assertEquals(2, r.rows().size());
        assertEquals("OLX", r.rows().get(0).source());
        assertEquals("EUR", r.rows().get(0).currency());
        assertEquals("id:OLX|a1", r.rows().get(0).dedupeKey());
    }

    @Test
    void dedupeKeyFallsBackToUrlWhenNoExternalId() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/9,Honda CBR600RR,6500,EUR,2024-05-01,,,,,,,\n";
        CsvParseResult r = parser.parse(csv);
        assertEquals(1, r.rows().size());
        assertTrue(r.rows().get(0).dedupeKey().startsWith("url:OLX|"));
    }

    @Test
    void requiredFieldsAreValidated() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/1,Honda,,EUR,2024-05-01,,,,,,,\n"; // missing price
        CsvParseResult r = parser.parse(csv);
        assertTrue(r.rows().isEmpty());
        assertTrue(r.errors().stream().anyMatch(e -> e.field().equals("price")));
    }

    @Test
    void rejectsNegativePriceBadCurrencyAndBadDate() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/1,Honda,-5,EURO,notadate,,,,,,,\n";
        CsvParseResult r = parser.parse(csv);
        assertTrue(r.rows().isEmpty());
        assertTrue(r.errors().stream().anyMatch(e -> e.field().equals("price")));
        assertTrue(r.errors().stream().anyMatch(e -> e.field().equals("currency")));
        assertTrue(r.errors().stream().anyMatch(e -> e.field().equals("observed_at")));
    }

    @Test
    void missingRequiredColumnIsReported() {
        String badHeader = "source,url,title,currency,observed_at\n"
                + "OLX,https://x.test/olx/1,Honda,EUR,2024-05-01\n";
        CsvParseResult r = parser.parse(badHeader);
        assertTrue(r.rows().isEmpty());
        assertTrue(r.errors().stream().anyMatch(e -> e.field().equals("price")));
    }

    @Test
    void validatesMileageNonNegativeInteger() {
        String csv = CsvContract.HEADER + "\n"
                + "OLX,https://x.test/olx/1,Honda,6500,EUR,2024-05-01,,,,,,-3,\n";
        CsvParseResult r = parser.parse(csv);
        assertTrue(r.errors().stream().anyMatch(e -> e.field().equals("mileage_km")));
    }
}
