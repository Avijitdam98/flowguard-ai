package com.flowguard.incident;

import com.flowguard.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident extends BaseEntity {

    @Column(name = "incident_number", nullable = false, unique = true, length = 50)
    private String incidentNumber;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(name = "affected_service_id")
    private UUID affectedServiceId;

    @Column(name = "related_api_id")
    private UUID relatedApiId;

    @Column(name = "assigned_engineer_id")
    private UUID assignedEngineerId;

    @Column(name = "assigned_engineer_name")
    private String assignedEngineerName;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Builder.Default
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<IncidentComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<IncidentEvent> events = new ArrayList<>();
}

