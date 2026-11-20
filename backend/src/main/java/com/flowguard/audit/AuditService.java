package com.flowguard.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditService {
    void log(UUID userId, String userEmail, String action, String resourceType, String resourceId, String details);
    Page<AuditLogDto> getAllLogs(Pageable pageable);
    Page<AuditLogDto> getLogsByUser(UUID userId, Pageable pageable);
    Page<AuditLogDto> getLogsByResourceType(String resourceType, Pageable pageable);
}

