package com.motointel.app.catalog;

import com.motointel.app.normalize.Normalizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogParserTest {

    private final CatalogParser parser = new CatalogParser(new Normalizer());

    private Document loadFixture() throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/sample_model.html")) {
            assertNotNull(in, "fixture must be on the classpath");
            return Jsoup.parse(in, "UTF-8", "");
        }
    }

    @Test
    void picksRichestSpecTableNotNav() throws Exception {
        List<String[]> pairs = parser.extractSpecPairs(loadFixture());
        // The spec table has 12 rows; the nav table only 1 — the parser must pick the spec table.
        assertTrue(pairs.size() >= 10, "should extract the spec table rows, not the nav table");
    }

    @Test
    void mapsSpecsWithUnitConversions() throws Exception {
        SpecValues spec = parser.parse(loadFixture());
        assertEquals(599.0, spec.engineCapacityCc().doubleValue(), 0.01);
        // 80 hp -> kW (the fixed conversion: x0.7457)
        assertEquals(59.66, spec.powerKw().doubleValue(), 0.01);
        // 9.2 kgf-m -> Nm
        assertEquals(90.22, spec.torqueNm().doubleValue(), 0.01);
        assertEquals(163.0, spec.dryWeightKg().doubleValue(), 0.01);
        // 32.3 in -> mm
        assertEquals(820.42, spec.seatHeightMm().doubleValue(), 0.01);
        assertEquals(18.1, spec.fuelCapacityL().doubleValue(), 0.01);
        // 160 mph -> km/h
        assertEquals(257.44, spec.topSpeedKmh().doubleValue(), 0.01);
        assertEquals("Liquid", spec.cooling());
        assertEquals("6 Speed", spec.transmission());
        assertEquals("Chain", spec.finalDrive());
    }
}
