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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApiMonitorRequest {

    @NotBlank(message = "API Monitor name is required")
    @Size(min = 2, max = 150)
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

    @Min(5)
    @Max(86400)
    private Integer monitoringIntervalSeconds;

    @Min(500)
    @Max(60000)
    private Integer timeoutMs;

    private Integer expectedHttpStatus;
    private Integer alertThresholdMs;
    private Boolean isActive;
}

