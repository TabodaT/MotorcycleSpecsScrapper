package com.motointel.app.analytics;

import com.motointel.app.dto.TimelineBucketDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Pure assembly of per-bucket timeline rows from three week&rarr;count maps. */
public final class TimelineAssembler {

    private TimelineAssembler() {}

    public static List<TimelineBucketDto> assemble(Map<LocalDate, Long> listed,
                                                   Map<LocalDate, Long> removed,
                                                   Map<LocalDate, Long> priceDrops) {
        TreeSet<LocalDate> buckets = new TreeSet<>();
        buckets.addAll(listed.keySet());
        buckets.addAll(removed.keySet());
        buckets.addAll(priceDrops.keySet());

        List<TimelineBucketDto> out = new ArrayList<>();
        for (LocalDate b : buckets) {
            out.add(new TimelineBucketDto(b.toString(),
                    listed.getOrDefault(b, 0L),
                    removed.getOrDefault(b, 0L),
                    priceDrops.getOrDefault(b, 0L)));
        }
        return out;
    }
}
