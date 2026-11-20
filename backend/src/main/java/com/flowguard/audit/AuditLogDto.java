package com.flowguard.audit;

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
public class AuditLogDto {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipAddress;
    private String userAgent;
    private String details;
    private Instant timestamp;

    public static AuditLogDto fromEntity(AuditLog log) {
        if (log == null) return null;
        return AuditLogDto.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userEmail(log.getUserEmail())
                .action(log.getAction())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .details(log.getDetails())
                .timestamp(log.getTimestamp())
                .build();
    }
}

