package com.flowguard.apimonitor.dto;

import com.flowguard.apimonitor.EnvironmentType;
import com.flowguard.apimonitor.HttpMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApiMonitorRequest {

    private UUID teamId;

    @NotBlank(message = "API Monitor name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Base URL is required")
    private String baseUrl;

    @NotBlank(message = "Endpoint path is required")
    private String endpointPath;

    @NotNull(message = "HTTP Method is required")
    private HttpMethod httpMethod;

    @NotNull(message = "Environment is required")
    private EnvironmentType environment;

    private String requestHeaders;
    private String requestBody;
    private String authConfig;

    @Min(value = 5, message = "Monitoring interval must be at least 5 seconds")
    @Max(value = 86400, message = "Monitoring interval cannot exceed 24 hours")
    @Builder.Default
    private Integer monitoringIntervalSeconds = 60;

    @Min(value = 500, message = "Timeout must be at least 500ms")
    @Max(value = 60000, message = "Timeout cannot exceed 60000ms")
    @Builder.Default
    private Integer timeoutMs = 5000;

    @Builder.Default
    private Integer expectedHttpStatus = 200;

    @Builder.Default
    private Integer alertThresholdMs = 2000;
}

