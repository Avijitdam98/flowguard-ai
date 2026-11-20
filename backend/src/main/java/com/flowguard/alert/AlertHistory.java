package com.flowguard.alert;

import com.flowguard.incident.IncidentSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alert_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "alert_rule_id", nullable = false)
    private UUID alertRuleId;

    @Column(name = "alert_rule_name")
    private String alertRuleName;

    @Column(name = "triggered_value", nullable = false)
    private Double triggeredValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity;

    @Builder.Default
    @Column(name = "is_acknowledged", nullable = false)
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "acknowledged_by_name")
    private String acknowledgedByName;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Builder.Default
    @Column(name = "triggered_at", nullable = false, updatable = false)
    private Instant triggeredAt = Instant.now();
}

