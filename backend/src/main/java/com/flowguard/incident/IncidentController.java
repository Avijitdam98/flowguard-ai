package com.flowguard.incident;

import com.flowguard.auth.UserPrincipal;
import com.flowguard.common.ApiResponse;
import com.flowguard.incident.dto.AddCommentRequest;
import com.flowguard.incident.dto.CreateIncidentRequest;
import com.flowguard.incident.dto.IncidentCommentDto;
import com.flowguard.incident.dto.IncidentDto;
import com.flowguard.incident.dto.IncidentEventDto;
import com.flowguard.incident.dto.UpdateIncidentStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incident Lifecycle Management", description = "Endpoints for triage, multi-engineer investigation collaboration, and audit trail timelines")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "List all incidents with status, severity, and search filtering")
    public ResponseEntity<ApiResponse<Page<IncidentDto>>> getAllIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<IncidentDto> incidents = incidentService.getAllIncidents(status, severity, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Incidents retrieved successfully", incidents));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed incident record including comments and timeline events")
    public ResponseEntity<ApiResponse<IncidentDto>> getIncidentById(@PathVariable UUID id) {
        IncidentDto incident = incidentService.getIncidentById(id);
        return ResponseEntity.ok(ApiResponse.ok("Incident retrieved successfully", incident));
    }

    @GetMapping("/by-number/{incidentNumber}")
    @Operation(summary = "Find incident by human-readable reference number (e.g. INC-2026-001)")
    public ResponseEntity<ApiResponse<IncidentDto>> getIncidentByNumber(@PathVariable String incidentNumber) {
        IncidentDto incident = incidentService.getIncidentByNumber(incidentNumber);
        return ResponseEntity.ok(ApiResponse.ok("Incident retrieved successfully", incident));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Declare a new engineering incident")
    public ResponseEntity<ApiResponse<IncidentDto>> createIncident(
            @Valid @RequestBody CreateIncidentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String actorName = principal != null ? principal.getEmail() : "Engineer";
        UUID actorId = principal != null ? principal.getId() : null;
        IncidentDto created = incidentService.createIncident(request, actorId, actorName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Incident declared successfully", created));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Update incident status, severity, or assignment")
    public ResponseEntity<ApiResponse<IncidentDto>> updateIncident(
            @PathVariable UUID id,
            @RequestBody UpdateIncidentStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String actorName = principal != null ? principal.getEmail() : "Engineer";
        UUID actorId = principal != null ? principal.getId() : null;
        IncidentDto updated = incidentService.updateIncidentStatus(id, request, actorId, actorName);
        return ResponseEntity.ok(ApiResponse.ok("Incident updated successfully", updated));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Post an investigation comment or triage note")
    public ResponseEntity<ApiResponse<IncidentCommentDto>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        String userName = principal != null ? principal.getEmail() : "Engineer";
        UUID userId = principal != null ? principal.getId() : null;
        IncidentCommentDto comment = incidentService.addComment(id, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Comment posted to incident log", comment));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List all comments on an incident")
    public ResponseEntity<ApiResponse<List<IncidentCommentDto>>> getComments(@PathVariable UUID id) {
        List<IncidentCommentDto> comments = incidentService.getComments(id);
        return ResponseEntity.ok(ApiResponse.ok("Comments retrieved", comments));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "List chronological audit events for an incident")
    public ResponseEntity<ApiResponse<List<IncidentEventDto>>> getTimeline(@PathVariable UUID id) {
        List<IncidentEventDto> timeline = incidentService.getTimelineEvents(id);
        return ResponseEntity.ok(ApiResponse.ok("Timeline events retrieved", timeline));
    }
}

