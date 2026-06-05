package com.motointel.app.analytics;

import com.motointel.app.dto.TimelineBucketDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimelineAssemblerTest {

    @Test
    void mergesAndSortsBuckets() {
        LocalDate w1 = LocalDate.of(2024, 4, 1);
        LocalDate w2 = LocalDate.of(2024, 4, 8);
        LocalDate w3 = LocalDate.of(2024, 4, 15);

        List<TimelineBucketDto> out = TimelineAssembler.assemble(
                Map.of(w1, 2L, w2, 3L),
                Map.of(w2, 1L),
                Map.of(w3, 5L));

        assertEquals(3, out.size());
        assertEquals("2024-04-01", out.get(0).bucket());
        assertEquals(2L, out.get(0).listed());
        assertEquals(0L, out.get(0).removed());

        assertEquals("2024-04-08", out.get(1).bucket());
        assertEquals(3L, out.get(1).listed());
        assertEquals(1L, out.get(1).removed());

        assertEquals("2024-04-15", out.get(2).bucket());
        assertEquals(5L, out.get(2).priceDrops());
        assertEquals(0L, out.get(2).listed());
    }

    @Test
    void emptyWhenNoData() {
        assertEquals(0, TimelineAssembler.assemble(Map.of(), Map.of(), Map.of()).size());
    }
}
