package com.flowguard.alert.dto;

import com.flowguard.alert.ComparisonOperator;
import com.flowguard.alert.MetricType;
import com.flowguard.alert.TargetType;
import com.flowguard.incident.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertRuleRequest {

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    private UUID targetId;

    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    @NotNull(message = "Comparison operator is required")
    private ComparisonOperator comparisonOperator;

    @NotNull(message = "Threshold value is required")
    private Double thresholdValue;

    private Integer evaluationWindowMinutes;

    private IncidentSeverity severity;

    private Boolean isEnabled;
}

