package com.flowguard.transaction.dto;

import com.flowguard.transaction.StepStatus;
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
public class RecordStepRequest {

    @NotBlank(message = "Service name is required")
    private String serviceName;

    @NotBlank(message = "Step name is required")
    private String stepName;

    @NotNull(message = "Step order is required")
    private Integer stepOrder;

    @NotNull(message = "Step status is required")
    private StepStatus status;

    @NotNull(message = "Duration in ms is required")
    private Integer durationMs;

    private String errorMessage;
    private String payloadSnapshot;
}

