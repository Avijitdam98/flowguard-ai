package com.flowguard.incident.dto;

import com.flowguard.incident.IncidentSeverity;
import com.flowguard.incident.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentStatusRequest {
    private IncidentStatus status;
    private IncidentSeverity severity;
    private UUID assignedEngineerId;
    private String assignedEngineerName;
    private String resolutionNotes;
}

