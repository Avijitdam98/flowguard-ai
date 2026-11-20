package com.flowguard.alert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistory, UUID> {
    Page<AlertHistory> findByIsAcknowledgedOrderByTriggeredAtDesc(Boolean isAcknowledged, Pageable pageable);
    List<AlertHistory> findTop10ByOrderByTriggeredAtDesc();
    long countByIsAcknowledgedFalse();
}

