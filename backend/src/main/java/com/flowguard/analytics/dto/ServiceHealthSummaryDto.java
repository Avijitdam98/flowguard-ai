package com.flowguard.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHealthSummaryDto {
    private UUID id;
    private String serviceName;
    private String environment;
    private String healthStatus;
    private double averageLatencyMs;
    private double uptimePercentage;
    private String lastHeartbeat;
}

