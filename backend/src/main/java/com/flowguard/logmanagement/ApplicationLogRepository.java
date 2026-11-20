package com.flowguard.logmanagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApplicationLogRepository extends JpaRepository<ApplicationLog, UUID>, JpaSpecificationExecutor<ApplicationLog> {

    Page<ApplicationLog> findByLogLevel(LogLevel logLevel, Pageable pageable);

    Page<ApplicationLog> findByServiceName(String serviceName, Pageable pageable);

    Page<ApplicationLog> findByCorrelationId(String correlationId, Pageable pageable);

    Page<ApplicationLog> findByTraceId(String traceId, Pageable pageable);

    @Query("SELECT DISTINCT a.serviceName FROM ApplicationLog a ORDER BY a.serviceName ASC")
    List<String> findDistinctServiceNames();

    @Query("SELECT a.logLevel, COUNT(a) FROM ApplicationLog a GROUP BY a.logLevel")
    List<Object[]> countGroupedByLogLevel();

    @Query("SELECT a FROM ApplicationLog a WHERE " +
           "(:serviceName IS NULL OR a.serviceName = :serviceName) AND " +
           "(:logLevel IS NULL OR a.logLevel = :logLevel) AND " +
           "(:search IS NULL OR LOWER(a.message) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.correlationId) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.traceId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.timestamp <= :endTime)")
    Page<ApplicationLog> searchLogs(
            @Param("serviceName") String serviceName,
            @Param("logLevel") LogLevel logLevel,
            @Param("search") String search,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            Pageable pageable
    );
}

