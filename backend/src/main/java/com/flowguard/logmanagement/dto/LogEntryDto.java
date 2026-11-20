package com.flowguard.logmanagement.dto;

import com.flowguard.logmanagement.ApplicationLog;
import com.flowguard.logmanagement.LogLevel;
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
public class LogEntryDto {
    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private String environment;
    private LogLevel logLevel;
    private String message;
    private String traceId;
    private String correlationId;
    private String exceptionDetails;
    private String loggerName;
    private Instant timestamp;
    private String metadata;

    public static LogEntryDto fromEntity(ApplicationLog log) {
        if (log == null) return null;
        return LogEntryDto.builder()
                .id(log.getId())
                .serviceId(log.getServiceId())
                .serviceName(log.getServiceName())
                .environment(log.getEnvironment())
                .logLevel(log.getLogLevel())
                .message(log.getMessage())
                .traceId(log.getTraceId())
                .correlationId(log.getCorrelationId())
                .exceptionDetails(log.getExceptionDetails())
                .loggerName(log.getLoggerName())
                .timestamp(log.getTimestamp())
                .metadata(log.getMetadata())
                .build();
    }
}

