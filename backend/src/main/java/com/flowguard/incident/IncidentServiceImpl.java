package com.flowguard.incident;

import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.incident.dto.AddCommentRequest;
import com.flowguard.incident.dto.CreateIncidentRequest;
import com.flowguard.incident.dto.IncidentCommentDto;
import com.flowguard.incident.dto.IncidentDto;
import com.flowguard.incident.dto.IncidentEventDto;
import com.flowguard.incident.dto.UpdateIncidentStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentCommentRepository commentRepository;
    private final IncidentEventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentDto> getAllIncidents(IncidentStatus status, IncidentSeverity severity, String search,
            Pageable pageable) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<Incident> page = incidentRepository.searchIncidents(status, severity, cleanSearch, pageable);
        return page.map(IncidentDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDto getIncidentById(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        return IncidentDto.fromEntity(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDto getIncidentByNumber(String incidentNumber) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "incidentNumber", incidentNumber));
        return IncidentDto.fromEntity(incident);
    }

    @Override
    @Transactional
    public IncidentDto createIncident(CreateIncidentRequest request, UUID actorId, String actorName) {
        long count = incidentRepository.count() + 1;
        String incidentNum = String.format("INC-%d-%03d", Year.now().getValue(), count);

        Incident incident = Incident.builder()
                .incidentNumber(incidentNum)
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(IncidentStatus.DETECTED)
                .affectedServiceId(request.getAffectedServiceId())
                .relatedApiId(request.getRelatedApiId())
                .assignedEngineerId(request.getAssignedEngineerId())
                .assignedEngineerName(request.getAssignedEngineerName())
                .build();

        Incident saved = incidentRepository.save(incident);

        // Record initial event
        IncidentEvent initialEvent = IncidentEvent.builder()
                .incident(saved)
                .eventType("INCIDENT_TRIGGERED")
                .eventDescription("Incident logged with severity " + saved.getSeverity() + " by "
                        + (actorName != null ? actorName : "Automated Alert Engine"))
                .actorId(actorId)
                .actorName(actorName)
                .createdAt(Instant.now())
                .build();
        eventRepository.save(initialEvent);

        return IncidentDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public IncidentDto updateIncidentStatus(UUID id, UpdateIncidentStatusRequest request, UUID actorId,
            String actorName) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        StringBuilder eventDesc = new StringBuilder("Incident updated: ");

        if (request.getStatus() != null && request.getStatus() != incident.getStatus()) {
            eventDesc.append("Status changed from ").append(incident.getStatus()).append(" to ")
                    .append(request.getStatus()).append(". ");
            incident.setStatus(request.getStatus());

            if (request.getStatus() == IncidentStatus.RESOLVED || request.getStatus() == IncidentStatus.CLOSED) {
                incident.setResolvedAt(Instant.now());
            }
        }

        if (request.getSeverity() != null && request.getSeverity() != incident.getSeverity()) {
            eventDesc.append("Severity updated to ").append(request.getSeverity()).append(". ");
            incident.setSeverity(request.getSeverity());
        }

        if (request.getAssignedEngineerId() != null) {
            incident.setAssignedEngineerId(request.getAssignedEngineerId());
            incident.setAssignedEngineerName(request.getAssignedEngineerName());
            eventDesc.append("Assigned to ").append(request.getAssignedEngineerName()).append(". ");
        }

        if (request.getResolutionNotes() != null) {
            incident.setResolutionNotes(request.getResolutionNotes());
        }

        Incident updated = incidentRepository.save(incident);

        IncidentEvent event = IncidentEvent.builder()
                .incident(updated)
                .eventType("STATUS_CHANGE")
                .eventDescription(eventDesc.toString())
                .actorId(actorId)
                .actorName(actorName)
                .createdAt(Instant.now())
                .build();
        eventRepository.save(event);

        return IncidentDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public IncidentCommentDto addComment(UUID incidentId, AddCommentRequest request, UUID userId, String userName) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        IncidentComment comment = IncidentComment.builder()
                .incident(incident)
                .userId(userId)
                .userName(userName != null ? userName : "Engineer")
                .commentText(request.getCommentText())
                .createdAt(Instant.now())
                .build();

        IncidentComment saved = commentRepository.save(comment);

        IncidentEvent event = IncidentEvent.builder()
                .incident(incident)
                .eventType("COMMENT_ADDED")
                .eventDescription((userName != null ? userName : "User") + " posted a note to the investigation log")
                .actorId(userId)
                .actorName(userName)
                .createdAt(Instant.now())
                .build();
        eventRepository.save(event);

        return IncidentCommentDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentCommentDto> getComments(UUID incidentId) {
        return commentRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(IncidentCommentDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentEventDto> getTimelineEvents(UUID incidentId) {
        return eventRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(IncidentEventDto::fromEntity)
                .collect(Collectors.toList());
    }
}
