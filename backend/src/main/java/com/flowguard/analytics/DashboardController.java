package com.flowguard.analytics;

import com.flowguard.analytics.dto.DashboardMetricsDto;
import com.flowguard.analytics.dto.TimeSeriesPointDto;
import com.flowguard.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Executive Dashboard", description = "Real-time monitoring metrics, telemetry trends, and operational health summaries")
public class DashboardController {

    private final AnalyticsService analyticsService;

    @GetMapping("/metrics")
    @Operation(summary = "Get aggregated executive dashboard metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsDto>> getDashboardMetrics(
            @RequestParam(defaultValue = "24h") String timeRange) {
        DashboardMetricsDto metrics = analyticsService.getDashboardMetrics(timeRange);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard metrics retrieved", metrics));
    }

    @GetMapping("/traffic")
    @Operation(summary = "Get API traffic time-series trends")
    public ResponseEntity<ApiResponse<List<TimeSeriesPointDto>>> getTrafficSeries(
            @RequestParam(defaultValue = "24h") String timeRange) {
        List<TimeSeriesPointDto> traffic = analyticsService.getTrafficSeries(timeRange);
        return ResponseEntity.ok(ApiResponse.ok("Traffic trend retrieved", traffic));
    }

    @GetMapping("/latency")
    @Operation(summary = "Get response time percentiles and latency trends")
    public ResponseEntity<ApiResponse<List<TimeSeriesPointDto>>> getLatencySeries(
            @RequestParam(defaultValue = "24h") String timeRange) {
        List<TimeSeriesPointDto> latency = analyticsService.getLatencySeries(timeRange);
        return ResponseEntity.ok(ApiResponse.ok("Latency trend retrieved", latency));
    }
}

