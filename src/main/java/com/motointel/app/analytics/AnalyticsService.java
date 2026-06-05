package com.motointel.app.analytics;

import com.motointel.app.dto.AnalyticsSummaryDto;
import com.motointel.app.dto.ModelAnalyticsDto;
import com.motointel.app.dto.PriceStatsDto;
import com.motointel.app.dto.TimelineBucketDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Analytics over market data. Median uses Postgres {@code percentile_cont(0.5)} (§4 [HARD]).
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final JdbcTemplate jdbc;

    public AnalyticsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AnalyticsSummaryDto summary() {
        long manufacturers = count("SELECT count(*) FROM manufacturers");
        long models = count("SELECT count(*) FROM motorcycle_models");
        long listings = count("SELECT count(*) FROM market_listings");
        long active = count("SELECT count(*) FROM market_listings WHERE status = 'active'");
        long removed = count("SELECT count(*) FROM market_listings WHERE status = 'removed'");
        long likelySold = count("SELECT count(*) FROM market_listings WHERE status = 'likely_sold'");
        long matched = count("SELECT count(*) FROM listing_model_matches WHERE status = 'matched'");
        long needsReview = count("SELECT count(*) FROM listing_model_matches WHERE status = 'needs_review'");
        long unmatched = count("SELECT count(*) FROM listing_model_matches WHERE status = 'unmatched'");

        return jdbc.queryForObject("""
                SELECT avg(price) AS avgp, min(price) AS minp, max(price) AS maxp,
                       percentile_cont(0.5) WITHIN GROUP (ORDER BY price) AS medianp
                FROM market_listings WHERE price IS NOT NULL
                """, (rs, n) -> new AnalyticsSummaryDto(
                manufacturers, models, listings, active, removed, likelySold,
                matched, needsReview, unmatched,
                scale(rs.getBigDecimal("avgp")), scale(rs.getBigDecimal("medianp")),
                rs.getBigDecimal("minp"), rs.getBigDecimal("maxp")));
    }

    public List<PriceStatsDto> prices(Long modelId) {
        StringBuilder sql = new StringBuilder("""
                SELECT mo.id AS model_id, mo.name AS model_name, count(l.id) AS cnt,
                       min(l.price) AS minp, max(l.price) AS maxp, avg(l.price) AS avgp,
                       percentile_cont(0.5) WITHIN GROUP (ORDER BY l.price) AS medianp
                FROM listing_model_matches m
                JOIN motorcycle_models mo ON mo.id = m.model_id
                JOIN market_listings l ON l.id = m.listing_id
                WHERE l.price IS NOT NULL AND m.model_id IS NOT NULL
                """);
        List<Object> args = new ArrayList<>();
        if (modelId != null) {
            sql.append(" AND m.model_id = ? ");
            args.add(modelId);
        }
        sql.append(" GROUP BY mo.id, mo.name ORDER BY cnt DESC, mo.name ASC");
        return jdbc.query(sql.toString(), (rs, n) -> new PriceStatsDto(
                rs.getLong("model_id"), rs.getString("model_name"), rs.getLong("cnt"),
                rs.getBigDecimal("minp"), rs.getBigDecimal("maxp"),
                scale(rs.getBigDecimal("avgp")), scale(rs.getBigDecimal("medianp"))), args.toArray());
    }

    public List<ModelAnalyticsDto> models() {
        return jdbc.query("""
                SELECT mo.id AS model_id, mo.name AS model_name,
                       count(*) FILTER (WHERE l.status = 'active') AS active_count,
                       count(*) AS total_observed
                FROM listing_model_matches m
                JOIN motorcycle_models mo ON mo.id = m.model_id
                JOIN market_listings l ON l.id = m.listing_id
                WHERE m.model_id IS NOT NULL
                GROUP BY mo.id, mo.name
                ORDER BY total_observed DESC, mo.name ASC
                """, (rs, n) -> new ModelAnalyticsDto(rs.getLong("model_id"), rs.getString("model_name"),
                rs.getLong("active_count"), rs.getLong("total_observed")));
    }

    public List<TimelineBucketDto> timeline(String bucket) {
        String unit = "month".equalsIgnoreCase(bucket) ? "month" : "week";
        Map<LocalDate, Long> listed = weeklyCounts(
                "SELECT date_trunc('" + unit + "', first_observed_at)::date AS wk, count(*) AS c "
                        + "FROM market_listings WHERE first_observed_at IS NOT NULL GROUP BY wk");
        Map<LocalDate, Long> removed = weeklyCounts(
                "SELECT date_trunc('" + unit + "', at)::date AS wk, count(*) AS c "
                        + "FROM listing_status_history WHERE status IN ('removed','likely_sold') GROUP BY wk");
        Map<LocalDate, Long> drops = weeklyCounts(
                "SELECT date_trunc('" + unit + "', observed_at)::date AS wk, count(*) AS c FROM ("
                        + "  SELECT observed_at, price, "
                        + "         lag(price) OVER (PARTITION BY listing_id ORDER BY observed_at) AS prev "
                        + "  FROM listing_price_history) t "
                        + "WHERE prev IS NOT NULL AND price < prev GROUP BY wk");

        return TimelineAssembler.assemble(listed, removed, drops);
    }

    private Map<LocalDate, Long> weeklyCounts(String sql) {
        Map<LocalDate, Long> map = new java.util.HashMap<>();
        jdbc.query(sql, rs -> {
            Date wk = rs.getDate("wk");
            if (wk != null) {
                map.put(wk.toLocalDate(), rs.getLong("c"));
            }
        });
        return map;
    }

    private long count(String sql) {
        Long v = jdbc.queryForObject(sql, Long.class);
        return v == null ? 0L : v;
    }

    private static BigDecimal scale(BigDecimal v) {
        return v == null ? null : v.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
