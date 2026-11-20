package com.flowguard.logmanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "application_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "service_id")
    private UUID serviceId;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String environment = "PRODUCTION";

    @Enumerated(EnumType.STRING)
    @Column(name = "log_level", nullable = false, length = 20)
    private LogLevel logLevel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "exception_details", columnDefinition = "TEXT")
    private String exceptionDetails;

    @Column(name = "logger_name", length = 255)
    private String loggerName;

    @Builder.Default
    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String metadata;
}

