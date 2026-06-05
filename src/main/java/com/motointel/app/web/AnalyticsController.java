package com.motointel.app.web;

import com.motointel.app.analytics.AnalyticsService;
import com.motointel.app.common.ApiResponse;
import com.motointel.app.dto.AnalyticsSummaryDto;
import com.motointel.app.dto.ModelAnalyticsDto;
import com.motointel.app.dto.PriceStatsDto;
import com.motointel.app.dto.TimelineBucketDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ApiResponse<AnalyticsSummaryDto> summary() {
        return ApiResponse.ok(analyticsService.summary());
    }

    @GetMapping("/prices")
    public ApiResponse<List<PriceStatsDto>> prices(@RequestParam(required = false) Long modelId) {
        return ApiResponse.ok(analyticsService.prices(modelId));
    }

    @GetMapping("/models")
    public ApiResponse<List<ModelAnalyticsDto>> models() {
        return ApiResponse.ok(analyticsService.models());
    }

    @GetMapping("/timeline")
    public ApiResponse<List<TimelineBucketDto>> timeline(@RequestParam(defaultValue = "week") String bucket) {
        return ApiResponse.ok(analyticsService.timeline(bucket));
    }
}
