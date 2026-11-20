package com.flowguard.apimonitor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, UUID> {
    Page<MonitoringResult> findByMonitorIdOrderByCheckedAtDesc(UUID monitorId, Pageable pageable);
    List<MonitoringResult> findTop10ByMonitorIdOrderByCheckedAtDesc(UUID monitorId);

    @Query("SELECT AVG(m.responseTimeMs) FROM MonitoringResult m WHERE m.monitorId = :monitorId")
    Double getAverageResponseTime(@Param("monitorId") UUID monitorId);

    @Query("SELECT COUNT(m) FROM MonitoringResult m WHERE m.monitorId = :monitorId AND m.isSuccessful = true")
    Long countSuccessfulChecks(@Param("monitorId") UUID monitorId);

    long countByIsSuccessful(boolean isSuccessful);

    @Query("SELECT AVG(m.responseTimeMs) FROM MonitoringResult m")
    Double getGlobalAverageResponseTime();

    @Query("SELECT m.responseTimeMs FROM MonitoringResult m ORDER BY m.responseTimeMs ASC")
    List<Integer> getAllResponseTimes();

    @Query("SELECT m.statusCode, COUNT(m) FROM MonitoringResult m GROUP BY m.statusCode")
    List<Object[]> getStatusCodeCounts();

    @Query("SELECT m FROM MonitoringResult m ORDER BY m.checkedAt DESC")
    List<MonitoringResult> findRecentResults(Pageable pageable);
}

