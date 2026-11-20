package com.flowguard.serviceregistry.dto;

import com.flowguard.apimonitor.MonitorStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {

    @NotBlank(message = "Service name is required")
    private String name;

    @NotBlank(message = "Slug is required")
    private String slug;

    private String description;

    private String environment;

    private MonitorStatus currentStatus;

    private UUID teamId;
}

