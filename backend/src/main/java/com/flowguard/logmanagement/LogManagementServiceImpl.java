package com.flowguard.logmanagement;

import com.flowguard.common.ResourceNotFoundException;
import com.flowguard.logmanagement.dto.BatchIngestLogRequest;
import com.flowguard.logmanagement.dto.IngestLogRequest;
import com.flowguard.logmanagement.dto.LogEntryDto;
import com.flowguard.logmanagement.dto.LogStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogManagementServiceImpl implements LogManagementService {

    private final ApplicationLogRepository logRepository;

    @Override
    @Transactional
    public LogEntryDto ingestLog(IngestLogRequest request) {
        ApplicationLog log = toEntity(request);
        ApplicationLog saved = logRepository.save(log);
        return LogEntryDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public List<LogEntryDto> ingestBatchLogs(BatchIngestLogRequest request) {
        List<ApplicationLog> entities = request.getLogs().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        List<ApplicationLog> saved = logRepository.saveAll(entities);
        return saved.stream().map(LogEntryDto::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LogEntryDto> searchLogs(String serviceName, LogLevel logLevel, String search, Instant startTime, Instant endTime, Pageable pageable) {
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanService = (serviceName != null && !serviceName.isBlank() && !serviceName.equalsIgnoreCase("ALL")) ? serviceName.trim() : null;
        Page<ApplicationLog> page = logRepository.searchLogs(cleanService, logLevel, cleanSearch, startTime, endTime, pageable);
        return page.map(LogEntryDto::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public LogEntryDto getLogById(UUID id) {
        ApplicationLog log = logRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationLog", "id", id));
        return LogEntryDto.fromEntity(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getDistinctServiceNames() {
        return logRepository.findDistinctServiceNames();
    }

    @Override
    @Transactional(readOnly = true)
    public LogStatsDto getLogStats() {
        List<Object[]> rows = logRepository.countGroupedByLogLevel();
        Map<String, Long> countMap = new LinkedHashMap<>();
        long total = 0;
        long errorCount = 0;
        long warningCount = 0;
        long infoCount = 0;

        for (Object[] r : rows) {
            LogLevel lvl = (LogLevel) r[0];
            Long cnt = (Long) r[1];
            countMap.put(lvl.name(), cnt);
            total += cnt;
            if (lvl == LogLevel.ERROR || lvl == LogLevel.FATAL) {
                errorCount += cnt;
            } else if (lvl == LogLevel.WARN) {
                warningCount += cnt;
            } else if (lvl == LogLevel.INFO) {
                infoCount += cnt;
            }
        }

        return LogStatsDto.builder()
                .totalLogs(total)
                .errorCount(errorCount)
                .warningCount(warningCount)
                .infoCount(infoCount)
                .countByLevel(countMap)
                .build();
    }

    private ApplicationLog toEntity(IngestLogRequest request) {
        return ApplicationLog.builder()
                .serviceId(request.getServiceId())
                .serviceName(request.getServiceName())
                .environment(request.getEnvironment() != null ? request.getEnvironment() : "PRODUCTION")
                .logLevel(request.getLogLevel())
                .message(request.getMessage())
                .traceId(request.getTraceId())
                .correlationId(request.getCorrelationId())
                .exceptionDetails(request.getExceptionDetails())
                .loggerName(request.getLoggerName())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now())
                .metadata(request.getMetadata())
                .build();
    }
}

