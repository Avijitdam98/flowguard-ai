package com.flowguard.logmanagement;

import com.flowguard.logmanagement.dto.BatchIngestLogRequest;
import com.flowguard.logmanagement.dto.IngestLogRequest;
import com.flowguard.logmanagement.dto.LogEntryDto;
import com.flowguard.logmanagement.dto.LogStatsDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LogManagementService {
    LogEntryDto ingestLog(IngestLogRequest request);
    List<LogEntryDto> ingestBatchLogs(BatchIngestLogRequest request);
    Page<LogEntryDto> searchLogs(String serviceName, LogLevel logLevel, String search, Instant startTime, Instant endTime, Pageable pageable);
    LogEntryDto getLogById(UUID id);
    List<String> getDistinctServiceNames();
    LogStatsDto getLogStats();
}

