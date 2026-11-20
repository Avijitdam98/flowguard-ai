package com.flowguard.alert;

import com.flowguard.alert.dto.AlertHistoryDto;
import com.flowguard.alert.dto.AlertRuleDto;
import com.flowguard.alert.dto.CreateAlertRuleRequest;
import com.flowguard.alert.dto.NotificationDto;
import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.incident.IncidentSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleRepository ruleRepository;
    private final AlertHistoryRepository historyRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AlertRuleDto> getAllRules(Boolean isEnabled, Pageable pageable) {
        Page<AlertRule> page = (isEnabled != null)
                ? ruleRepository.findByIsEnabled(isEnabled, pageable)
                : ruleRepository.findAll(pageable);
        return page.map(AlertRuleDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertRuleDto getRuleById(UUID id) {
        AlertRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", "id", id));
        return AlertRuleDto.fromEntity(rule);
    }

    @Override
    @Transactional
    public AlertRuleDto createRule(CreateAlertRuleRequest request) {
        AlertRule rule = AlertRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .metricType(request.getMetricType())
                .comparisonOperator(request.getComparisonOperator())
                .thresholdValue(request.getThresholdValue())
                .evaluationWindowMinutes(request.getEvaluationWindowMinutes() != null ? request.getEvaluationWindowMinutes() : 5)
                .severity(request.getSeverity() != null ? request.getSeverity() : IncidentSeverity.MEDIUM)
                .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .build();

        AlertRule saved = ruleRepository.save(rule);
        return AlertRuleDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public AlertRuleDto toggleRule(UUID id) {
        AlertRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertRule", "id", id));
        rule.setIsEnabled(!rule.getIsEnabled());
        AlertRule saved = ruleRepository.save(rule);
        return AlertRuleDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("AlertRule", "id", id);
        }
        ruleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlertHistoryDto> getAlertHistory(Boolean isAcknowledged, Pageable pageable) {
        Page<AlertHistory> page = (isAcknowledged != null)
                ? historyRepository.findByIsAcknowledgedOrderByTriggeredAtDesc(isAcknowledged, pageable)
                : historyRepository.findAll(pageable);
        return page.map(AlertHistoryDto::fromEntity);
    }

    @Override
    @Transactional
    public AlertHistoryDto acknowledgeAlert(UUID id, UUID userId, String userName) {
        AlertHistory alert = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertHistory", "id", id));

        alert.setIsAcknowledged(true);
        alert.setAcknowledgedBy(userId);
        alert.setAcknowledgedByName(userName != null ? userName : "Engineer");
        alert.setAcknowledgedAt(Instant.now());

        AlertHistory saved = historyRepository.save(alert);
        return AlertHistoryDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnacknowledgedAlerts() {
        return historyRepository.countByIsAcknowledgedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(String recipientEmail, Pageable pageable) {
        Page<Notification> page = (recipientEmail != null && !recipientEmail.isBlank())
                ? notificationRepository.findByRecipientEmailOrderBySentAtDesc(recipientEmail, pageable)
                : notificationRepository.findAll(pageable);
        return page.map(NotificationDto::fromEntity);
    }
}

