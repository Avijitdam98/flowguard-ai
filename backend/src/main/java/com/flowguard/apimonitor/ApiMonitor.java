package com.flowguard.apimonitor;

import com.flowguard.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_monitors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiMonitor extends BaseEntity {

    @Column(name = "team_id")
    private UUID teamId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Column(name = "endpoint_path", nullable = false, length = 500)
    private String endpointPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 10)
    private HttpMethod httpMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EnvironmentType environment;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "auth_config", columnDefinition = "TEXT")
    private String authConfig;

    @Builder.Default
    @Column(name = "monitoring_interval_seconds", nullable = false)
    private Integer monitoringIntervalSeconds = 60;

    @Builder.Default
    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs = 5000;

    @Builder.Default
    @Column(name = "expected_http_status", nullable = false)
    private Integer expectedHttpStatus = 200;

    @Builder.Default
    @Column(name = "alert_threshold_ms", nullable = false)
    private Integer alertThresholdMs = 2000;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private MonitorStatus currentStatus = MonitorStatus.UNKNOWN;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    public String getFullUrl() {
        if (baseUrl.endsWith("/") && endpointPath.startsWith("/")) {
            return baseUrl + endpointPath.substring(1);
        } else if (!baseUrl.endsWith("/") && !endpointPath.startsWith("/")) {
            return baseUrl + "/" + endpointPath;
        }
        return baseUrl + endpointPath;
    }
}

