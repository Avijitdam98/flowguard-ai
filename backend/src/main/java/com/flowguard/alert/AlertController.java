package com.flowguard.alert;

import com.flowguard.alert.dto.AlertHistoryDto;
import com.flowguard.alert.dto.AlertRuleDto;
import com.flowguard.alert.dto.CreateAlertRuleRequest;
import com.flowguard.alert.dto.NotificationDto;
import com.flowguard.auth.UserPrincipal;
import com.flowguard.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alert Engine & Notifications", description = "Threshold evaluation rules, multi-channel alerting, and notification dispatch")
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/rules")
    @Operation(summary = "List all configured threshold alert rules")
    public ResponseEntity<ApiResponse<Page<AlertRuleDto>>> getAllRules(
            @RequestParam(required = false) Boolean isEnabled,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AlertRuleDto> rules = alertService.getAllRules(isEnabled, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Alert rules retrieved", rules));
    }

    @GetMapping("/rules/{id}")
    @Operation(summary = "Get alert rule by ID")
    public ResponseEntity<ApiResponse<AlertRuleDto>> getRuleById(@PathVariable UUID id) {
        AlertRuleDto rule = alertService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.ok("Alert rule retrieved", rule));
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Create a new threshold alert rule")
    public ResponseEntity<ApiResponse<AlertRuleDto>> createRule(@Valid @RequestBody CreateAlertRuleRequest request) {
        AlertRuleDto created = alertService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Alert rule created", created));
    }

    @PatchMapping("/rules/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Enable or disable an alert rule")
    public ResponseEntity<ApiResponse<AlertRuleDto>> toggleRule(@PathVariable UUID id) {
        AlertRuleDto toggled = alertService.toggleRule(id);
        return ResponseEntity.ok(ApiResponse.ok("Alert rule status toggled", toggled));
    }

    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete an alert rule")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable UUID id) {
        alertService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.ok("Alert rule deleted", null));
    }

    @GetMapping("/history")
    @Operation(summary = "List triggered alert events history")
    public ResponseEntity<ApiResponse<Page<AlertHistoryDto>>> getAlertHistory(
            @RequestParam(required = false) Boolean isAcknowledged,
            @PageableDefault(size = 20, sort = "triggeredAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AlertHistoryDto> history = alertService.getAlertHistory(isAcknowledged, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Alert history retrieved", history));
    }

    @PatchMapping("/history/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Acknowledge an active alert event")
    public ResponseEntity<ApiResponse<AlertHistoryDto>> acknowledgeAlert(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userName = principal != null ? principal.getEmail() : "Engineer";
        UUID userId = principal != null ? principal.getId() : null;
        AlertHistoryDto acknowledged = alertService.acknowledgeAlert(id, userId, userName);
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged", acknowledged));
    }

    @GetMapping("/unacknowledged/count")
    @Operation(summary = "Get count of unacknowledged alerts for badge indicators")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnacknowledged() {
        long count = alertService.countUnacknowledgedAlerts();
        return ResponseEntity.ok(ApiResponse.ok("Unacknowledged count", Map.of("count", count)));
    }

    @GetMapping("/notifications")
    @Operation(summary = "List dispatched notifications for user")
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationDto> notifications = alertService.getNotifications(email, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", notifications));
    }
}

