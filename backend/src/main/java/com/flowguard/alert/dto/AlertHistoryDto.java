package com.flowguard.alert.dto;

import com.flowguard.alert.AlertHistory;
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
public class AlertHistoryDto {
    private UUID id;
    private UUID alertRuleId;
    private String alertRuleName;
    private Double triggeredValue;
    private String message;
    private IncidentSeverity severity;
    private Boolean isAcknowledged;
    private UUID acknowledgedBy;
    private String acknowledgedByName;
    private Instant acknowledgedAt;
    private Instant triggeredAt;

    public static AlertHistoryDto fromEntity(AlertHistory history) {
        if (history == null) return null;
        return AlertHistoryDto.builder()
                .id(history.getId())
                .alertRuleId(history.getAlertRuleId())
                .alertRuleName(history.getAlertRuleName())
                .triggeredValue(history.getTriggeredValue())
                .message(history.getMessage())
                .severity(history.getSeverity())
                .isAcknowledged(history.getIsAcknowledged())
                .acknowledgedBy(history.getAcknowledgedBy())
                .acknowledgedByName(history.getAcknowledgedByName())
                .acknowledgedAt(history.getAcknowledgedAt())
                .triggeredAt(history.getTriggeredAt())
                .build();
    }
}

