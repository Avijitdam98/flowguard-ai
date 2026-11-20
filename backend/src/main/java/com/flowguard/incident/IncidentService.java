package com.flowguard.incident;

import com.flowguard.incident.dto.AddCommentRequest;
import com.flowguard.incident.dto.CreateIncidentRequest;
import com.flowguard.incident.dto.IncidentCommentDto;
import com.flowguard.incident.dto.IncidentDto;
import com.flowguard.incident.dto.IncidentEventDto;
import com.flowguard.incident.dto.UpdateIncidentStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IncidentService {
    Page<IncidentDto> getAllIncidents(IncidentStatus status, IncidentSeverity severity, String search, Pageable pageable);
    IncidentDto getIncidentById(UUID id);
    IncidentDto getIncidentByNumber(String incidentNumber);
    IncidentDto createIncident(CreateIncidentRequest request, UUID actorId, String actorName);
    IncidentDto updateIncidentStatus(UUID id, UpdateIncidentStatusRequest request, UUID actorId, String actorName);
    IncidentCommentDto addComment(UUID incidentId, AddCommentRequest request, UUID userId, String userName);
    List<IncidentCommentDto> getComments(UUID incidentId);
    List<IncidentEventDto> getTimelineEvents(UUID incidentId);
}

