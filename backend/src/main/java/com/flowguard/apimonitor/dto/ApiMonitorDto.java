package com.flowguard.apimonitor.dto;

import com.flowguard.apimonitor.ApiMonitor;
import com.flowguard.apimonitor.EnvironmentType;
import com.flowguard.apimonitor.HttpMethod;
import com.flowguard.apimonitor.MonitorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMonitorDto {
    private UUID id;
    private UUID teamId;
    private String name;
    private String baseUrl;
    private String endpointPath;
    private String fullUrl;
    private HttpMethod httpMethod;
    private EnvironmentType environment;
    private String requestHeaders;
    private String requestBody;
    private String authConfig;
    private Integer monitoringIntervalSeconds;
    private Integer timeoutMs;
    private Integer expectedHttpStatus;
    private Integer alertThresholdMs;
    private Boolean isActive;
    private MonitorStatus currentStatus;
    private Instant lastCheckedAt;
    private Double averageLatencyMs;
    private Double uptimePercentage;
    private Instant createdAt;
    private Instant updatedAt;

    public static ApiMonitorDto fromEntity(ApiMonitor monitor) {
        if (monitor == null) return null;
        return ApiMonitorDto.builder()
                .id(monitor.getId())
                .teamId(monitor.getTeamId())
                .name(monitor.getName())
                .baseUrl(monitor.getBaseUrl())
                .endpointPath(monitor.getEndpointPath())
                .fullUrl(monitor.getFullUrl())
                .httpMethod(monitor.getHttpMethod())
                .environment(monitor.getEnvironment())
                .requestHeaders(monitor.getRequestHeaders())
                .requestBody(monitor.getRequestBody())
                .authConfig(monitor.getAuthConfig())
                .monitoringIntervalSeconds(monitor.getMonitoringIntervalSeconds())
                .timeoutMs(monitor.getTimeoutMs())
                .expectedHttpStatus(monitor.getExpectedHttpStatus())
                .alertThresholdMs(monitor.getAlertThresholdMs())
                .isActive(monitor.getIsActive())
                .currentStatus(monitor.getCurrentStatus())
                .lastCheckedAt(monitor.getLastCheckedAt())
                .createdAt(monitor.getCreatedAt())
                .updatedAt(monitor.getUpdatedAt())
                .build();
    }
}

