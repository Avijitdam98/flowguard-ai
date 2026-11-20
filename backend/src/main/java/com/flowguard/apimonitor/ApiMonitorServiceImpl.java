package com.flowguard.apimonitor;

import com.flowguard.apimonitor.dto.ApiMonitorDto;
import com.flowguard.apimonitor.dto.CreateApiMonitorRequest;
import com.flowguard.apimonitor.dto.MonitoringResultDto;
import com.flowguard.apimonitor.dto.UpdateApiMonitorRequest;
import com.flowguard.audit.AuditService;
import com.flowguard.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiMonitorServiceImpl implements ApiMonitorService {

    private final ApiMonitorRepository monitorRepository;
    private final MonitoringResultRepository resultRepository;
    private final MonitoringExecutionService executionService;
    private final SsrfProtectionValidator ssrfValidator;
    private final AuditService auditService;

    @Override
    @Transactional
    public ApiMonitorDto createMonitor(CreateApiMonitorRequest request, UUID actorId) {
        String fullUrl = request.getBaseUrl() + request.getEndpointPath();
        ssrfValidator.validateUrl(fullUrl);

        ApiMonitor monitor = ApiMonitor.builder()
                .teamId(request.getTeamId())
                .name(request.getName())
                .baseUrl(request.getBaseUrl().trim())
                .endpointPath(request.getEndpointPath().trim())
                .httpMethod(request.getHttpMethod())
                .environment(request.getEnvironment())
                .requestHeaders(request.getRequestHeaders())
                .requestBody(request.getRequestBody())
                .authConfig(request.getAuthConfig())
                .monitoringIntervalSeconds(request.getMonitoringIntervalSeconds())
                .timeoutMs(request.getTimeoutMs())
                .expectedHttpStatus(request.getExpectedHttpStatus())
                .alertThresholdMs(request.getAlertThresholdMs())
                .isActive(true)
                .currentStatus(MonitorStatus.UNKNOWN)
                .build();

        ApiMonitor saved = monitorRepository.save(monitor);
        auditService.log(actorId, null, "CREATE_MONITOR", "API_MONITOR", saved.getId().toString(), "Created monitor: " + saved.getName());
        return enrichDto(saved);
    }

    @Override
    @Transactional
    public ApiMonitorDto updateMonitor(UUID id, UpdateApiMonitorRequest request, UUID actorId) {
        ApiMonitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiMonitor", "id", id));

        String fullUrl = request.getBaseUrl() + request.getEndpointPath();
        ssrfValidator.validateUrl(fullUrl);

        monitor.setName(request.getName());
        monitor.setBaseUrl(request.getBaseUrl().trim());
        monitor.setEndpointPath(request.getEndpointPath().trim());
        monitor.setHttpMethod(request.getHttpMethod());
        monitor.setEnvironment(request.getEnvironment());
        monitor.setRequestHeaders(request.getRequestHeaders());
        monitor.setRequestBody(request.getRequestBody());
        monitor.setAuthConfig(request.getAuthConfig());

        if (request.getMonitoringIntervalSeconds() != null) {
            monitor.setMonitoringIntervalSeconds(request.getMonitoringIntervalSeconds());
        }
        if (request.getTimeoutMs() != null) {
            monitor.setTimeoutMs(request.getTimeoutMs());
        }
        if (request.getExpectedHttpStatus() != null) {
            monitor.setExpectedHttpStatus(request.getExpectedHttpStatus());
        }
        if (request.getAlertThresholdMs() != null) {
            monitor.setAlertThresholdMs(request.getAlertThresholdMs());
        }
        if (request.getIsActive() != null) {
            monitor.setIsActive(request.getIsActive());
        }

        ApiMonitor updated = monitorRepository.save(monitor);
        auditService.log(actorId, null, "UPDATE_MONITOR", "API_MONITOR", id.toString(), "Updated monitor: " + updated.getName());
        return enrichDto(updated);
    }

    @Override
    @Transactional
    public void deleteMonitor(UUID id, UUID actorId) {
        ApiMonitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiMonitor", "id", id));

        monitorRepository.delete(monitor);
        auditService.log(actorId, null, "DELETE_MONITOR", "API_MONITOR", id.toString(), "Deleted monitor: " + monitor.getName());
    }

    @Override
    @Transactional
    public ApiMonitorDto toggleMonitor(UUID id, UUID actorId) {
        ApiMonitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiMonitor", "id", id));

        monitor.setIsActive(!monitor.getIsActive());
        ApiMonitor saved = monitorRepository.save(monitor);
        auditService.log(actorId, null, "TOGGLE_MONITOR", "API_MONITOR", id.toString(), "Toggled active state to: " + saved.getIsActive());
        return enrichDto(saved);
    }

    @Override
    @Transactional
    public MonitoringResultDto triggerProbe(UUID id) {
        ApiMonitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiMonitor", "id", id));
        return executionService.executeProbe(monitor);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiMonitorDto getMonitorById(UUID id) {
        ApiMonitor monitor = monitorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApiMonitor", "id", id));
        return enrichDto(monitor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApiMonitorDto> getAllMonitors(Pageable pageable, EnvironmentType environment, MonitorStatus status) {
        Page<ApiMonitor> monitors;
        if (environment != null) {
            monitors = monitorRepository.findByEnvironment(environment, pageable);
        } else if (status != null) {
            monitors = monitorRepository.findByCurrentStatus(status, pageable);
        } else {
            monitors = monitorRepository.findAll(pageable);
        }
        return monitors.map(this::enrichDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MonitoringResultDto> getMonitorHistory(UUID id, Pageable pageable) {
        if (!monitorRepository.existsById(id)) {
            throw new ResourceNotFoundException("ApiMonitor", "id", id);
        }
        return resultRepository.findByMonitorIdOrderByCheckedAtDesc(id, pageable)
                .map(MonitoringResultDto::fromEntity);
    }

    private ApiMonitorDto enrichDto(ApiMonitor monitor) {
        ApiMonitorDto dto = ApiMonitorDto.fromEntity(monitor);
        Double avgLatency = resultRepository.getAverageResponseTime(monitor.getId());
        dto.setAverageLatencyMs(avgLatency != null ? Math.round(avgLatency * 10.0) / 10.0 : 0.0);

        Long total = resultRepository.countTotalChecks(monitor.getId());
        Long successful = resultRepository.countSuccessfulChecks(monitor.getId());
        if (total != null && total > 0 && successful != null) {
            double uptime = ((double) successful / total) * 100.0;
            dto.setUptimePercentage(Math.round(uptime * 100.0) / 100.0);
        } else {
            dto.setUptimePercentage(100.0);
        }
        return dto;
    }
}

