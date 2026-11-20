package com.flowguard.analytics;

import com.flowguard.analytics.dto.DashboardMetricsDto;
import com.flowguard.analytics.dto.TimeSeriesPointDto;

import java.util.List;

public interface AnalyticsService {
    DashboardMetricsDto getDashboardMetrics(String timeRange);
    List<TimeSeriesPointDto> getTrafficSeries(String timeRange);
    List<TimeSeriesPointDto> getLatencySeries(String timeRange);
}

