package com.flowguard.incident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Optional<Incident> findByIncidentNumber(String incidentNumber);

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    Page<Incident> findBySeverity(IncidentSeverity severity, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:severity IS NULL OR i.severity = :severity) AND " +
           "(:search IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.incidentNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Incident> searchIncidents(
            @Param("status") IncidentStatus status,
            @Param("severity") IncidentSeverity severity,
            @Param("search") String search,
            Pageable pageable
    );

    long countByStatus(IncidentStatus status);

    long countBySeverity(IncidentSeverity severity);
}

