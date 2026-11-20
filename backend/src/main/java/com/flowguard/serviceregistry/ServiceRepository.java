package com.flowguard.serviceregistry;

import com.flowguard.apimonitor.MonitorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    Optional<ServiceEntity> findBySlugAndEnvironment(String slug, String environment);
    Page<ServiceEntity> findByEnvironment(String environment, Pageable pageable);
    Page<ServiceEntity> findByCurrentStatus(MonitorStatus currentStatus, Pageable pageable);
    long countByCurrentStatus(MonitorStatus status);
}

