package com.flowguard.incident.dto;

import com.flowguard.incident.Incident;
import com.flowguard.incident.IncidentSeverity;
import com.flowguard.incident.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    private UUID id;
    private String incidentNumber;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private UUID affectedServiceId;
    private UUID relatedApiId;
    private UUID assignedEngineerId;
    private String assignedEngineerName;
    private String resolutionNotes;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<IncidentCommentDto> comments;
    private List<IncidentEventDto> events;

    public static IncidentDto fromEntity(Incident incident) {
        if (incident == null) return null;
        return IncidentDto.builder()
                .id(incident.getId())
                .incidentNumber(incident.getIncidentNumber())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .affectedServiceId(incident.getAffectedServiceId())
                .relatedApiId(incident.getRelatedApiId())
                .assignedEngineerId(incident.getAssignedEngineerId())
                .assignedEngineerName(incident.getAssignedEngineerName())
                .resolutionNotes(incident.getResolutionNotes())
                .resolvedAt(incident.getResolvedAt())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .comments(incident.getComments() != null ? incident.getComments().stream()
                        .map(IncidentCommentDto::fromEntity)
                        .collect(Collectors.toList()) : null)
                .events(incident.getEvents() != null ? incident.getEvents().stream()
                        .map(IncidentEventDto::fromEntity)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}

