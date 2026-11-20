package com.flowguard.incident.dto;

import com.flowguard.incident.IncidentEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEventDto {
    private UUID id;
    private UUID incidentId;
    private String eventType;
    private String eventDescription;
    private UUID actorId;
    private String actorName;
    private Instant createdAt;

    public static IncidentEventDto fromEntity(IncidentEvent event) {
        if (event == null) return null;
        return IncidentEventDto.builder()
                .id(event.getId())
                .incidentId(event.getIncident() != null ? event.getIncident().getId() : null)
                .eventType(event.getEventType())
                .eventDescription(event.getEventDescription())
                .actorId(event.getActorId())
                .actorName(event.getActorName())
                .createdAt(event.getCreatedAt())
                .build();
    }
}

