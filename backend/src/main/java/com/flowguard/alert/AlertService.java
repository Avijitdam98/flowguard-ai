package com.flowguard.alert;

import com.flowguard.alert.dto.AlertHistoryDto;
import com.flowguard.alert.dto.AlertRuleDto;
import com.flowguard.alert.dto.CreateAlertRuleRequest;
import com.flowguard.alert.dto.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AlertService {
    Page<AlertRuleDto> getAllRules(Boolean isEnabled, Pageable pageable);
    AlertRuleDto getRuleById(UUID id);
    AlertRuleDto createRule(CreateAlertRuleRequest request);
    AlertRuleDto toggleRule(UUID id);
    void deleteRule(UUID id);

    Page<AlertHistoryDto> getAlertHistory(Boolean isAcknowledged, Pageable pageable);
    AlertHistoryDto acknowledgeAlert(UUID id, UUID userId, String userName);
    long countUnacknowledgedAlerts();

    Page<NotificationDto> getNotifications(String recipientEmail, Pageable pageable);
}

