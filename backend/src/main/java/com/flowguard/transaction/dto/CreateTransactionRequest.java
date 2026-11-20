package com.flowguard.transaction.dto;

import com.flowguard.transaction.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Correlation ID is required")
    private String correlationId;

    private String requestId;

    @NotNull(message = "Transaction status is required")
    private TransactionStatus transactionStatus;

    private String rootServiceName;
    private Long totalDurationMs;
    private String failureReason;
    private String metadata;
}

