package com.flowguard.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDto {

    // KPI Cards
    private long totalApiRequests;
    private long successfulRequests;
    private long failedRequests;
    private double averageResponseTimeMs;
    private double p95ResponseTimeMs;
    private double apiAvailabilityPercentage;
    private int activeIncidents;
    private double transactionSuccessRate;

    // Charts
    private List<TimeSeriesPointDto> trafficTrend;
    private List<TimeSeriesPointDto> latencyTrend;
    private List<TimeSeriesPointDto> errorRateTrend;
    private Map<String, Integer> serviceHealthDistribution;
    private Map<String, Long> statusDistribution;

    // Detailed Summaries
    private List<FailingApiSummaryDto> topFailingApis;
    private List<FailingApiSummaryDto> slowestApis;
    private List<ServiceHealthSummaryDto> serviceHealthOverview;
    private List<RecentIncidentSummaryDto> recentIncidents;
    private List<RecentTransactionSummaryDto> recentTransactions;
}

