package com.flowguard.transaction.dto;

import com.flowguard.transaction.StepStatus;
import com.flowguard.transaction.TransactionStep;
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
public class TransactionStepDto {
    private UUID id;
    private UUID transactionId;
    private String serviceName;
    private String stepName;
    private Integer stepOrder;
    private StepStatus status;
    private Integer durationMs;
    private String errorMessage;
    private String payloadSnapshot;
    private Instant executedAt;

    public static TransactionStepDto fromEntity(TransactionStep step) {
        if (step == null) return null;
        return TransactionStepDto.builder()
                .id(step.getId())
                .transactionId(step.getTransaction() != null ? step.getTransaction().getId() : null)
                .serviceName(step.getServiceName())
                .stepName(step.getStepName())
                .stepOrder(step.getStepOrder())
                .status(step.getStatus())
                .durationMs(step.getDurationMs())
                .errorMessage(step.getErrorMessage())
                .payloadSnapshot(step.getPayloadSnapshot())
                .executedAt(step.getExecutedAt())
                .build();
    }
}

