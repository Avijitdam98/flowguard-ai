package com.flowguard.analytics;

import com.flowguard.analytics.dto.DashboardMetricsDto;
import com.flowguard.analytics.dto.FailingApiSummaryDto;
import com.flowguard.analytics.dto.RecentIncidentSummaryDto;
import com.flowguard.analytics.dto.RecentTransactionSummaryDto;
import com.flowguard.analytics.dto.ServiceHealthSummaryDto;
import com.flowguard.analytics.dto.TimeSeriesPointDto;
import com.flowguard.apimonitor.ApiMonitor;
import com.flowguard.apimonitor.ApiMonitorRepository;
import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.apimonitor.MonitoringResult;
import com.flowguard.apimonitor.MonitoringResultRepository;
import com.flowguard.incident.Incident;
import com.flowguard.incident.IncidentRepository;
import com.flowguard.incident.IncidentStatus;
import com.flowguard.serviceregistry.ServiceEntity;
import com.flowguard.serviceregistry.ServiceRepository;
import com.flowguard.transaction.Transaction;
import com.flowguard.transaction.TransactionRepository;
import com.flowguard.transaction.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final ApiMonitorRepository monitorRepository;
    private final MonitoringResultRepository resultRepository;
    private final TransactionRepository transactionRepository;
    private final IncidentRepository incidentRepository;
    private final ServiceRepository serviceRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("UTC"));
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricsDto getDashboardMetrics(String timeRange) {
        log.info("Calculating dynamic dashboard metrics for timeRange: {}", timeRange);

        // 1. Calculate Real API Monitoring Counts
        long totalApiRequests = resultRepository.count();
        long successfulRequests = resultRepository.countByIsSuccessful(true);
        long failedRequests = resultRepository.countByIsSuccessful(false);

        Double avgResponseTime = resultRepository.getGlobalAverageResponseTime();
        double averageResponseTimeMs = avgResponseTime != null ? Math.round(avgResponseTime * 10.0) / 10.0 : 0.0;

        List<Integer> allTimes = resultRepository.getAllResponseTimes();
        double p95ResponseTimeMs = 0.0;
        if (!allTimes.isEmpty()) {
            int p95Index = Math.min(allTimes.size() - 1, (int) Math.ceil(0.95 * allTimes.size()) - 1);
            p95ResponseTimeMs = allTimes.get(p95Index);
        }

        double apiAvailabilityPercentage = totalApiRequests > 0
                ? Math.round(((double) successfulRequests / totalApiRequests) * 10000.0) / 100.0
                : 0.0;

        // 2. Incident Metrics
        int activeIncidents = (int) (
                incidentRepository.countByStatus(IncidentStatus.INVESTIGATING)
                        + incidentRepository.countByStatus(IncidentStatus.DETECTED)
                        + incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED)
        );

        // 3. Transaction Metrics
        long totalTransactions = transactionRepository.count();
        long successfulTransactions = transactionRepository.countByTransactionStatus(TransactionStatus.SUCCESS);
        double transactionSuccessRate = totalTransactions > 0
                ? Math.round(((double) successfulTransactions / totalTransactions) * 10000.0) / 100.0
                : 0.0;

        // 4. Service Health Distribution from Database
        Map<String, Integer> serviceHealthDist = new LinkedHashMap<>();
        serviceHealthDist.put("HEALTHY", (int) serviceRepository.countByCurrentStatus(MonitorStatus.HEALTHY));
        serviceHealthDist.put("DEGRADED", (int) serviceRepository.countByCurrentStatus(MonitorStatus.DEGRADED));
        serviceHealthDist.put("UNHEALTHY", (int) serviceRepository.countByCurrentStatus(MonitorStatus.UNHEALTHY));
        serviceHealthDist.put("UNKNOWN", (int) serviceRepository.countByCurrentStatus(MonitorStatus.UNKNOWN));

        // 5. HTTP Status Code Distribution from Database
        Map<String, Long> statusDist = new LinkedHashMap<>();
        long count2xx = 0;
        long count4xx = 0;
        long count5xx = 0;
        for (Object[] row : resultRepository.getStatusCodeCounts()) {
            Integer code = (Integer) row[0];
            Long count = (Long) row[1];
            if (code != null) {
                if (code >= 200 && code < 300) {
                    count2xx += count;
                } else if (code >= 400 && code < 500) {
                    count4xx += count;
                } else if (code >= 500) {
                    count5xx += count;
                }
            }
        }
        statusDist.put("Successful (2xx)", count2xx);
        statusDist.put("Client Errors (4xx)", count4xx);
        statusDist.put("Server Errors (5xx)", count5xx);

        // 6. Dynamic Time Series Trends
        List<MonitoringResult> recentResults = resultRepository.findRecentResults(PageRequest.of(0, 1000));
        List<TimeSeriesPointDto> traffic = computeDynamicTrafficTrend(recentResults);
        List<TimeSeriesPointDto> latency = computeDynamicLatencyTrend(recentResults);
        List<TimeSeriesPointDto> errorRate = computeDynamicErrorRateTrend(recentResults);

        // 7. Top Failing APIs and Slowest APIs
        List<ApiMonitor> allMonitors = monitorRepository.findAll();
        List<FailingApiSummaryDto> topFailing = computeTopFailingApis(allMonitors);
        List<FailingApiSummaryDto> slowest = computeSlowestApis(allMonitors);

        // 8. Service Health Overview
        List<ServiceHealthSummaryDto> serviceHealth = computeServiceHealthOverview();

        // 9. Recent Incidents
        List<RecentIncidentSummaryDto> incidents = computeRecentIncidents();

        // 10. Recent Transactions
        List<RecentTransactionSummaryDto> transactions = computeRecentTransactions();

        return DashboardMetricsDto.builder()
                .totalApiRequests(totalApiRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .averageResponseTimeMs(averageResponseTimeMs)
                .p95ResponseTimeMs(p95ResponseTimeMs)
                .apiAvailabilityPercentage(apiAvailabilityPercentage)
                .activeIncidents(activeIncidents)
                .transactionSuccessRate(transactionSuccessRate)
                .trafficTrend(traffic)
                .latencyTrend(latency)
                .errorRateTrend(errorRate)
                .serviceHealthDistribution(serviceHealthDist)
                .statusDistribution(statusDist)
                .topFailingApis(topFailing)
                .slowestApis(slowest)
                .serviceHealthOverview(serviceHealth)
                .recentIncidents(incidents)
                .recentTransactions(transactions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSeriesPointDto> getTrafficSeries(String timeRange) {
        List<MonitoringResult> recentResults = resultRepository.findRecentResults(PageRequest.of(0, 1000));
        return computeDynamicTrafficTrend(recentResults);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSeriesPointDto> getLatencySeries(String timeRange) {
        List<MonitoringResult> recentResults = resultRepository.findRecentResults(PageRequest.of(0, 1000));
        return computeDynamicLatencyTrend(recentResults);
    }

    private List<TimeSeriesPointDto> computeDynamicTrafficTrend(List<MonitoringResult> results) {
        List<TimeSeriesPointDto> list = new ArrayList<>();
        if (results.isEmpty()) {
            return list;
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Map<String, long[]> hourlyBuckets = new LinkedHashMap<>();

        // Initialize 12 hourly buckets
        for (int i = 11; i >= 0; i--) {
            Instant bucketTime = now.minus(i * 2, ChronoUnit.HOURS);
            String label = TIME_FORMATTER.format(bucketTime);
            hourlyBuckets.put(label, new long[]{0, 0}); // [success, fail]
        }

        for (MonitoringResult res : results) {
            String label = TIME_FORMATTER.format(res.getCheckedAt());
            if (hourlyBuckets.containsKey(label)) {
                long[] counts = hourlyBuckets.get(label);
                if (Boolean.TRUE.equals(res.getIsSuccessful())) {
                    counts[0]++;
                } else {
                    counts[1]++;
                }
            }
        }

        for (Map.Entry<String, long[]> entry : hourlyBuckets.entrySet()) {
            list.add(TimeSeriesPointDto.builder()
                    .timestamp(entry.getKey())
                    .value(entry.getValue()[0])
                    .secondaryValue((double) entry.getValue()[1])
                    .label("Traffic (req/bucket)")
                    .build());
        }

        return list;
    }

    private List<TimeSeriesPointDto> computeDynamicLatencyTrend(List<MonitoringResult> results) {
        List<TimeSeriesPointDto> list = new ArrayList<>();
        if (results.isEmpty()) {
            return list;
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Map<String, List<Integer>> latencyBuckets = new LinkedHashMap<>();

        for (int i = 11; i >= 0; i--) {
            Instant bucketTime = now.minus(i * 2, ChronoUnit.HOURS);
            String label = TIME_FORMATTER.format(bucketTime);
            latencyBuckets.put(label, new ArrayList<>());
        }

        for (MonitoringResult res : results) {
            String label = TIME_FORMATTER.format(res.getCheckedAt());
            if (latencyBuckets.containsKey(label) && res.getResponseTimeMs() != null) {
                latencyBuckets.get(label).add(res.getResponseTimeMs());
            }
        }

        for (Map.Entry<String, List<Integer>> entry : latencyBuckets.entrySet()) {
            List<Integer> times = entry.getValue();
            double avg = 0.0;
            double p95 = 0.0;
            if (!times.isEmpty()) {
                times.sort(Integer::compareTo);
                avg = times.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                int p95Idx = Math.min(times.size() - 1, (int) Math.ceil(0.95 * times.size()) - 1);
                p95 = times.get(p95Idx);
            }
            list.add(TimeSeriesPointDto.builder()
                    .timestamp(entry.getKey())
                    .value(Math.round(avg * 10.0) / 10.0)
                    .secondaryValue((double) Math.round(p95))
                    .label("Latency (ms)")
                    .build());
        }

        return list;
    }

    private List<TimeSeriesPointDto> computeDynamicErrorRateTrend(List<MonitoringResult> results) {
        List<TimeSeriesPointDto> list = new ArrayList<>();
        if (results.isEmpty()) {
            return list;
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Map<String, int[]> errorBuckets = new LinkedHashMap<>();

        for (int i = 11; i >= 0; i--) {
            Instant bucketTime = now.minus(i * 2, ChronoUnit.HOURS);
            String label = TIME_FORMATTER.format(bucketTime);
            errorBuckets.put(label, new int[]{0, 0}); // [failCount, totalCount]
        }

        for (MonitoringResult res : results) {
            String label = TIME_FORMATTER.format(res.getCheckedAt());
            if (errorBuckets.containsKey(label)) {
                int[] counts = errorBuckets.get(label);
                counts[1]++;
                if (Boolean.FALSE.equals(res.getIsSuccessful())) {
                    counts[0]++;
                }
            }
        }

        for (Map.Entry<String, int[]> entry : errorBuckets.entrySet()) {
            int[] c = entry.getValue();
            double rate = c[1] > 0 ? Math.round(((double) c[0] / c[1]) * 1000.0) / 10.0 : 0.0;
            list.add(TimeSeriesPointDto.builder()
                    .timestamp(entry.getKey())
                    .value(rate)
                    .label("Error Rate (%)")
                    .build());
        }

        return list;
    }

    private List<FailingApiSummaryDto> computeTopFailingApis(List<ApiMonitor> monitors) {
        List<FailingApiSummaryDto> dtoList = new ArrayList<>();

        for (ApiMonitor monitor : monitors) {
            Long total = resultRepository.countTotalChecks(monitor.getId());
            if (total != null && total > 0) {
                Long successful = resultRepository.countSuccessfulChecks(monitor.getId());
                long failCount = total - (successful != null ? successful : 0);
                double failureRate = Math.round(((double) failCount / total) * 1000.0) / 10.0;

                List<MonitoringResult> topRecent = resultRepository.findTop10ByMonitorIdOrderByCheckedAtDesc(monitor.getId());
                int lastStatusCode = 200;
                String lastError = null;
                if (!topRecent.isEmpty()) {
                    lastStatusCode = topRecent.get(0).getStatusCode();
                    lastError = topRecent.get(0).getErrorMessage();
                }

                if (failCount > 0 || monitor.getCurrentStatus() != MonitorStatus.HEALTHY) {
                    dtoList.add(FailingApiSummaryDto.builder()
                            .id(monitor.getId())
                            .name(monitor.getName())
                            .endpoint(monitor.getEndpointPath())
                            .method(monitor.getHttpMethod().name())
                            .failureCount((int) failCount)
                            .failureRate(failureRate)
                            .lastStatusCode(lastStatusCode)
                            .lastErrorMessage(lastError != null ? lastError : "Status: " + monitor.getCurrentStatus())
                            .build());
                }
            }
        }

        dtoList.sort(Comparator.comparingInt(FailingApiSummaryDto::getFailureCount).reversed());
        return dtoList.stream().limit(5).collect(Collectors.toList());
    }

    private List<FailingApiSummaryDto> computeSlowestApis(List<ApiMonitor> monitors) {
        List<FailingApiSummaryDto> dtoList = new ArrayList<>();

        for (ApiMonitor monitor : monitors) {
            Double avgResp = resultRepository.getAverageResponseTime(monitor.getId());
            if (avgResp != null) {
                Long total = resultRepository.countTotalChecks(monitor.getId());
                Long successful = resultRepository.countSuccessfulChecks(monitor.getId());
                long failCount = (total != null ? total : 0) - (successful != null ? successful : 0);
                double failureRate = total != null && total > 0 ? Math.round(((double) failCount / total) * 1000.0) / 10.0 : 0.0;

                dtoList.add(FailingApiSummaryDto.builder()
                        .id(monitor.getId())
                        .name(monitor.getName())
                        .endpoint(monitor.getEndpointPath())
                        .method(monitor.getHttpMethod().name())
                        .failureCount((int) failCount)
                        .failureRate(failureRate)
                        .lastStatusCode(200)
                        .lastErrorMessage("Avg: " + Math.round(avgResp) + "ms (Threshold: " + monitor.getAlertThresholdMs() + "ms)")
                        .build());
            }
        }

        dtoList.sort((a, b) -> {
            double latencyA = extractLatency(a.getLastErrorMessage());
            double latencyB = extractLatency(b.getLastErrorMessage());
            return Double.compare(latencyB, latencyA);
        });

        return dtoList.stream().limit(5).collect(Collectors.toList());
    }

    private double extractLatency(String msg) {
        if (msg == null) return 0.0;
        try {
            int start = msg.indexOf("Avg: ");
            int end = msg.indexOf("ms", start);
            if (start >= 0 && end > start) {
                return Double.parseDouble(msg.substring(start + 5, end).trim());
            }
        } catch (Exception ignored) {}
        return 0.0;
    }

    private List<ServiceHealthSummaryDto> computeServiceHealthOverview() {
        List<ServiceEntity> services = serviceRepository.findAll();
        List<ServiceHealthSummaryDto> dtoList = new ArrayList<>();
        Double globalAvg = resultRepository.getGlobalAverageResponseTime();
        double fallbackAvg = globalAvg != null ? Math.round(globalAvg * 10.0) / 10.0 : 0.0;
        long totalResults = resultRepository.count();
        long successResults = resultRepository.countByIsSuccessful(true);
        double globalUptime = totalResults > 0 ? Math.round(((double) successResults / totalResults) * 10000.0) / 100.0 : 100.0;

        for (ServiceEntity service : services) {
            String timeAgo = formatTimeAgo(service.getUpdatedAt() != null ? service.getUpdatedAt() : service.getCreatedAt());
            double serviceUptime = service.getCurrentStatus() == MonitorStatus.HEALTHY 
                    ? globalUptime 
                    : (service.getCurrentStatus() == MonitorStatus.DEGRADED ? Math.max(0.0, globalUptime - 15.0) : 0.0);

            dtoList.add(ServiceHealthSummaryDto.builder()
                    .id(service.getId())
                    .serviceName(service.getName())
                    .environment(service.getEnvironment())
                    .healthStatus(service.getCurrentStatus() != null ? service.getCurrentStatus().name() : "UNKNOWN")
                    .averageLatencyMs(fallbackAvg)
                    .uptimePercentage(Math.round(serviceUptime * 100.0) / 100.0)
                    .lastHeartbeat(timeAgo)
                    .build());
        }

        return dtoList;
    }

    private List<RecentIncidentSummaryDto> computeRecentIncidents() {
        List<Incident> recent = incidentRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        List<RecentIncidentSummaryDto> dtoList = new ArrayList<>();
        for (Incident inc : recent) {
            String serviceName = "Core Platform";
            if (inc.getAffectedServiceId() != null) {
                serviceName = serviceRepository.findById(inc.getAffectedServiceId())
                        .map(ServiceEntity::getName)
                        .orElse("Service-" + inc.getAffectedServiceId().toString().substring(0, 8));
            }

            dtoList.add(RecentIncidentSummaryDto.builder()
                    .id(inc.getId())
                    .incidentNumber(inc.getIncidentNumber())
                    .title(inc.getTitle())
                    .severity(inc.getSeverity().name())
                    .status(inc.getStatus().name())
                    .affectedService(serviceName)
                    .createdAt(formatTimeAgo(inc.getCreatedAt()))
                    .build());
        }

        return dtoList;
    }

    private List<RecentTransactionSummaryDto> computeRecentTransactions() {
        List<Transaction> recent = transactionRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "startTime"))
        ).getContent();

        List<RecentTransactionSummaryDto> dtoList = new ArrayList<>();
        for (Transaction tx : recent) {
            dtoList.add(RecentTransactionSummaryDto.builder()
                    .id(tx.getId())
                    .transactionId(tx.getTransactionId())
                    .correlationId(tx.getCorrelationId())
                    .status(tx.getTransactionStatus() != null ? tx.getTransactionStatus().name() : "UNKNOWN")
                    .durationMs(tx.getTotalDurationMs() != null ? tx.getTotalDurationMs() : 0L)
                    .rootService(tx.getRootServiceName() != null ? tx.getRootServiceName() : "API Gateway")
                    .startTime(formatTimeAgo(tx.getStartTime()))
                    .build());
        }

        return dtoList;
    }

    private String formatTimeAgo(Instant time) {
        if (time == null) return "recently";
        Duration d = Duration.between(time, Instant.now());
        long seconds = d.getSeconds();
        if (seconds < 60) return seconds + "s ago";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        return days + "d ago";
    }
}
