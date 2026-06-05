package com.motointel.app.normalize;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalizerTest {

    private final Normalizer n = new Normalizer();

    private static double d(Optional<BigDecimal> v) {
        return v.orElseThrow().doubleValue();
    }

    @Test
    void unitConversions() {
        assertEquals(74.57, n.hpToKw(100).doubleValue(), 0.01);
        assertEquals(73.55, n.psToKw(100).doubleValue(), 0.01);
        assertEquals(9.81, n.kgmToNm(1).doubleValue(), 0.01);
        assertEquals(1.36, n.ftLbToNm(1).doubleValue(), 0.01);
        assertEquals(25.40, n.inchToMm(1).doubleValue(), 0.01);
        assertEquals(0.45, n.lbToKg(1).doubleValue(), 0.01);
        assertEquals(1.61, n.mphToKmh(1).doubleValue(), 0.01);
        assertEquals(5.00, n.mpgToL100km(47.042).doubleValue(), 0.01);
    }

    @Test
    void hpToKwBugIsFixed() {
        // Legacy bug: it returned the raw hp number. Correct: hp * 0.7457.
        BigDecimal kw = n.parsePowerKw("80 hp @ 11000 rpm").orElseThrow();
        assertEquals(59.66, kw.doubleValue(), 0.01);
        assertNotEquals(80.0, kw.doubleValue(), 0.01);
    }

    @Test
    void parsePowerKwRecognisesUnits() {
        assertEquals(88.00, d(n.parsePowerKw("88 kW")), 0.01);
        assertEquals(73.55, d(n.parsePowerKw("100 PS")), 0.01);
        assertEquals(74.57, d(n.parsePowerKw("100 bhp")), 0.01);
    }

    @Test
    void parseTorqueNm() {
        assertEquals(66.00, d(n.parseTorqueNm("66 Nm")), 0.01);
        assertEquals(90.22, d(n.parseTorqueNm("9.2 kgf-m @ 9000 rpm")), 0.01);
        assertEquals(65.08, d(n.parseTorqueNm("48 ft-lb")), 0.01);
    }

    @Test
    void parseLengthWeightSpeedConsumption() {
        assertEquals(820.42, d(n.parseLengthMm("32.3 in")), 0.01);
        assertEquals(820.00, d(n.parseLengthMm("820 mm")), 0.01);
        assertEquals(181.44, d(n.parseWeightKg("400 lb")), 0.01);
        assertEquals(163.00, d(n.parseWeightKg("163 kg")), 0.01);
        assertEquals(257.44, d(n.parseSpeedKmh("160 mph")), 0.01);
        assertEquals(260.00, d(n.parseSpeedKmh("260 km/h")), 0.01);
        assertEquals(5.00, d(n.parseConsumptionL100km("47 mpg")), 0.01);
        assertEquals(4.20, d(n.parseConsumptionL100km("4.2 l/100km")), 0.01);
    }

    @Test
    void capacityFromModelName() {
        assertEquals(600, n.capacityFromModelName("CBR600RR").orElseThrow());
        assertEquals(750, n.capacityFromModelName("GSX-R 750").orElseThrow());
        assertTrue(n.capacityFromModelName("MT-07").isEmpty());
        assertTrue(n.capacityFromModelName("YZF-R6").isEmpty());
    }

    @Test
    void electricDetection() {
        assertTrue(n.isElectric("Zero SR electric motorcycle"));
        assertTrue(n.isElectric("Battery 21 kWh"));
        assertFalse(n.isElectric("Honda CBR600RR petrol"));
    }

    @Test
    void yearRangeParsing() {
        assertEquals(new YearRange(2003, 2006), n.parseYearRange("2003 - 2006"));
        assertEquals(new YearRange(2003, 2006), n.parseYearRange("2003-06"));
        assertEquals(new YearRange(1998, 1998), n.parseYearRange("1998"));
        assertEquals(YearRange.empty(), n.parseYearRange(""));
    }

    @Test
    void extraction() {
        assertEquals(2008, n.extractYear("Honda 2008 model low km").orElseThrow());
        assertEquals(32000, n.extractMileageKm("32 000 km, one owner").orElseThrow());
        assertEquals(32180, n.extractMileageKm("20,000 miles").orElseThrow());
    }

    @Test
    void textNormalization() {
        assertEquals("cbr 600 rr", n.normalizeText("  CBR 600 RR! "));
        assertEquals("cbr600rr", n.compact("CBR-600-RR"));
        assertEquals(List.of("cbr", "600", "rr"), n.tokens("CBR 600 RR"));
        assertEquals("", n.normalizeText(null));
    }

    @Test
    void detectManufacturer() {
        assertEquals("honda", n.detectManufacturer("Honda CBR600RR 2008",
                List.of("honda", "yamaha", "suzuki")).orElseThrow());
        assertTrue(n.detectManufacturer("Generic 600 supersport", List.of("honda", "yamaha")).isEmpty());
    }
}
