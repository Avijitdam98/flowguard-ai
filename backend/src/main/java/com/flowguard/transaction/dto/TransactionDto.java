package com.flowguard.transaction.dto;

import com.flowguard.transaction.Transaction;
import com.flowguard.transaction.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private UUID id;
    private String transactionId;
    private String correlationId;
    private String requestId;
    private TransactionStatus transactionStatus;
    private Instant startTime;
    private Instant endTime;
    private Long totalDurationMs;
    private String rootServiceName;
    private String failureReason;
    private String metadata;
    private Instant createdAt;
    private List<TransactionStepDto> steps;

    public static TransactionDto fromEntity(Transaction tx) {
        if (tx == null) return null;
        return TransactionDto.builder()
                .id(tx.getId())
                .transactionId(tx.getTransactionId())
                .correlationId(tx.getCorrelationId())
                .requestId(tx.getRequestId())
                .transactionStatus(tx.getTransactionStatus())
                .startTime(tx.getStartTime())
                .endTime(tx.getEndTime())
                .totalDurationMs(tx.getTotalDurationMs())
                .rootServiceName(tx.getRootServiceName())
                .failureReason(tx.getFailureReason())
                .metadata(tx.getMetadata())
                .createdAt(tx.getCreatedAt())
                .steps(tx.getSteps() != null ? tx.getSteps().stream()
                        .map(TransactionStepDto::fromEntity)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}

