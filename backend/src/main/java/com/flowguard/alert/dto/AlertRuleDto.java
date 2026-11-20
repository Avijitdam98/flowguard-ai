package com.flowguard.alert.dto;

import com.flowguard.alert.AlertRule;
import com.flowguard.alert.ComparisonOperator;
import com.flowguard.alert.MetricType;
import com.flowguard.alert.TargetType;
import com.flowguard.incident.IncidentSeverity;
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
public class AlertRuleDto {
    private UUID id;
    private String name;
    private String description;
    private TargetType targetType;
    private UUID targetId;
    private MetricType metricType;
    private ComparisonOperator comparisonOperator;
    private Double thresholdValue;
    private Integer evaluationWindowMinutes;
    private IncidentSeverity severity;
    private Boolean isEnabled;
    private Instant createdAt;
    private Instant updatedAt;

    public static AlertRuleDto fromEntity(AlertRule rule) {
        if (rule == null) return null;
        return AlertRuleDto.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .targetType(rule.getTargetType())
                .targetId(rule.getTargetId())
                .metricType(rule.getMetricType())
                .comparisonOperator(rule.getComparisonOperator())
                .thresholdValue(rule.getThresholdValue())
                .evaluationWindowMinutes(rule.getEvaluationWindowMinutes())
                .severity(rule.getSeverity())
                .isEnabled(rule.getIsEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}

