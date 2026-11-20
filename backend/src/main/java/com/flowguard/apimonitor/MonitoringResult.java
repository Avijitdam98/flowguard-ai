package com.flowguard.apimonitor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitoring_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "response_time_ms", nullable = false)
    private Integer responseTimeMs;

    @Column(name = "is_successful", nullable = false)
    private Boolean isSuccessful;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    @Column(name = "response_body_sample", columnDefinition = "TEXT")
    private String responseBodySample;

    @Builder.Default
    @Column(name = "checked_at", nullable = false, updatable = false)
    private Instant checkedAt = Instant.now();
}

