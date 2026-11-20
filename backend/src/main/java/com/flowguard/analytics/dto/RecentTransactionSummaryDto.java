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
public class RecentTransactionSummaryDto {
    private UUID id;
    private String transactionId;
    private String correlationId;
    private String status;
    private long durationMs;
    private String rootService;
    private String startTime;
}

