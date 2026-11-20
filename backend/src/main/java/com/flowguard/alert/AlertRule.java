package com.flowguard.alert;

import com.flowguard.common.BaseEntity;
import com.flowguard.incident.IncidentSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "alert_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 50)
    private TargetType targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private MetricType metricType;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_operator", nullable = false, length = 20)
    private ComparisonOperator comparisonOperator;

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Builder.Default
    @Column(name = "evaluation_window_minutes", nullable = false)
    private Integer evaluationWindowMinutes = 5;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity = IncidentSeverity.MEDIUM;

    @Builder.Default
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
}

