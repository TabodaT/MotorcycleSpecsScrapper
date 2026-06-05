package com.motointel.app.web;

import com.motointel.app.config.AppProperties;
import com.motointel.app.dto.HealthDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bare (unwrapped) health endpoint (§4 exception) so uptime checks / smoke script can parse it.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbc;
    private final AppProperties props;

    public HealthController(JdbcTemplate jdbc, AppProperties props) {
        this.jdbc = jdbc;
        this.props = props;
    }

    @GetMapping
    public HealthDto health() {
        String db;
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            db = "UP";
        } catch (Exception e) {
            db = "DOWN";
        }
        return new HealthDto("UP", db, props.getVersion());
    }
}
