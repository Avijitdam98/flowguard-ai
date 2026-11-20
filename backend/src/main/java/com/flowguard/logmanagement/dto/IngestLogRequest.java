package com.flowguard.logmanagement.dto;

import com.flowguard.logmanagement.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class IngestLogRequest {

    private UUID serviceId;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String environment;

    @NotNull(message = "Log level is required")
    private LogLevel logLevel;

    @NotBlank(message = "Log message is required")
    private String message;

    private String traceId;

    private String correlationId;

    private String exceptionDetails;

    private String loggerName;

    private Instant timestamp;

    private String metadata;
}

