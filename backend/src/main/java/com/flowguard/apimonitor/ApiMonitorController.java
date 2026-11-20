package com.flowguard.apimonitor;

import com.flowguard.apimonitor.dto.ApiMonitorDto;
import com.flowguard.apimonitor.dto.CreateApiMonitorRequest;
import com.flowguard.apimonitor.dto.MonitoringResultDto;
import com.flowguard.apimonitor.dto.UpdateApiMonitorRequest;
import com.flowguard.auth.UserPrincipal;
import com.flowguard.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monitors")
@RequiredArgsConstructor
@Tag(name = "API Monitoring", description = "Endpoints for managing monitored APIs, SSRF-guarded health probes, and uptime telemetry")
public class ApiMonitorController {

    private final ApiMonitorService monitorService;

    @GetMapping
    @Operation(summary = "List all monitored APIs with optional status or environment filters")
    public ResponseEntity<ApiResponse<Page<ApiMonitorDto>>> getAllMonitors(
            @RequestParam(required = false) EnvironmentType environment,
            @RequestParam(required = false) MonitorStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApiMonitorDto> monitors = monitorService.getAllMonitors(pageable, environment, status);
        return ResponseEntity.ok(ApiResponse.ok("Monitors retrieved successfully", monitors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get monitor details by ID")
    public ResponseEntity<ApiResponse<ApiMonitorDto>> getMonitorById(@PathVariable UUID id) {
        ApiMonitorDto monitor = monitorService.getMonitorById(id);
        return ResponseEntity.ok(ApiResponse.ok("Monitor retrieved successfully", monitor));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Register a new monitored API with SSRF safety checks")
    public ResponseEntity<ApiResponse<ApiMonitorDto>> createMonitor(
            @Valid @RequestBody CreateApiMonitorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ApiMonitorDto created = monitorService.createMonitor(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Monitor created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Update an existing monitored API configuration")
    public ResponseEntity<ApiResponse<ApiMonitorDto>> updateMonitor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApiMonitorRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ApiMonitorDto updated = monitorService.updateMonitor(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Monitor updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete an API monitor (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteMonitor(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        monitorService.deleteMonitor(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Monitor deleted successfully", null));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Enable or disable automated probing for a monitor")
    public ResponseEntity<ApiResponse<ApiMonitorDto>> toggleMonitor(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        ApiMonitorDto updated = monitorService.toggleMonitor(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Monitor status toggled successfully", updated));
    }

    @PostMapping("/{id}/probe")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Execute an immediate health check probe")
    public ResponseEntity<ApiResponse<MonitoringResultDto>> triggerProbe(@PathVariable UUID id) {
        MonitoringResultDto result = monitorService.triggerProbe(id);
        return ResponseEntity.ok(ApiResponse.ok("Health probe executed", result));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Retrieve historical health check probe results")
    public ResponseEntity<ApiResponse<Page<MonitoringResultDto>>> getMonitorHistory(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MonitoringResultDto> history = monitorService.getMonitorHistory(id, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Monitor history retrieved", history));
    }
}

