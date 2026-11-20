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
public class FailingApiSummaryDto {
    private UUID id;
    private String name;
    private String endpoint;
    private String method;
    private int failureCount;
    private double failureRate;
    private int lastStatusCode;
    private String lastErrorMessage;
}

