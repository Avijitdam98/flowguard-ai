package com.flowguard.incident.dto;

import com.flowguard.incident.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Severity is required")
    private IncidentSeverity severity;

    private UUID affectedServiceId;

    private UUID relatedApiId;

    private UUID assignedEngineerId;

    private String assignedEngineerName;
}

