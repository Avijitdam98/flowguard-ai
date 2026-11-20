package com.flowguard.apimonitor;

import com.flowguard.apimonitor.dto.ApiMonitorDto;
import com.flowguard.apimonitor.dto.CreateApiMonitorRequest;
import com.flowguard.apimonitor.dto.MonitoringResultDto;
import com.flowguard.apimonitor.dto.UpdateApiMonitorRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApiMonitorService {
    ApiMonitorDto createMonitor(CreateApiMonitorRequest request, UUID actorId);
    ApiMonitorDto updateMonitor(UUID id, UpdateApiMonitorRequest request, UUID actorId);
    void deleteMonitor(UUID id, UUID actorId);
    ApiMonitorDto toggleMonitor(UUID id, UUID actorId);
    MonitoringResultDto triggerProbe(UUID id);
    ApiMonitorDto getMonitorById(UUID id);
    Page<ApiMonitorDto> getAllMonitors(Pageable pageable, EnvironmentType environment, MonitorStatus status);
    Page<MonitoringResultDto> getMonitorHistory(UUID id, Pageable pageable);
}

