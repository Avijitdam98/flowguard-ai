package com.flowguard.apimonitor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApiMonitorRepository extends JpaRepository<ApiMonitor, UUID> {
    List<ApiMonitor> findByIsActiveTrue();
    Page<ApiMonitor> findByEnvironment(EnvironmentType environment, Pageable pageable);
    Page<ApiMonitor> findByCurrentStatus(MonitorStatus currentStatus, Pageable pageable);
    long countByCurrentStatus(MonitorStatus status);
}

