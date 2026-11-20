package com.flowguard.audit;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);
    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional
    public void log(UUID userId, String userEmail, String action, String resourceType, String resourceId, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("Audit record created: {} on {} by {}", action, resourceType, userEmail);
        } catch (Exception ex) {
            log.error("Failed to write audit log: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .map(AuditLogDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getLogsByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(AuditLogDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getLogsByResourceType(String resourceType, Pageable pageable) {
        return auditLogRepository.findByResourceTypeOrderByTimestampDesc(resourceType, pageable)
                .map(AuditLogDto::fromEntity);
    }
}

