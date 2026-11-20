package com.flowguard.serviceregistry.dto;

import com.flowguard.apimonitor.MonitorStatus;
import com.flowguard.serviceregistry.ServiceEntity;
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
public class ServiceDto {
    private UUID id;
    private UUID teamId;
    private String name;
    private String slug;
    private String description;
    private String environment;
    private MonitorStatus currentStatus;
    private Instant createdAt;
    private Instant updatedAt;

    public static ServiceDto fromEntity(ServiceEntity entity) {
        if (entity == null) return null;
        return ServiceDto.builder()
                .id(entity.getId())
                .teamId(entity.getTeamId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .environment(entity.getEnvironment())
                .currentStatus(entity.getCurrentStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

