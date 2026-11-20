package com.flowguard.logmanagement;

import com.flowguard.common.ApiResponse;
import com.flowguard.logmanagement.dto.BatchIngestLogRequest;
import com.flowguard.logmanagement.dto.IngestLogRequest;
import com.flowguard.logmanagement.dto.LogEntryDto;
import com.flowguard.logmanagement.dto.LogStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Centralized Log Management", description = "High-throughput log ingestion, full-text search, and multi-service trace correlation")
public class LogManagementController {

    private final LogManagementService logService;

    @GetMapping
    @Operation(summary = "Search and filter application logs across services, levels, and correlation IDs")
    public ResponseEntity<ApiResponse<Page<LogEntryDto>>> searchLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,
            @PageableDefault(size = 50, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LogEntryDto> logs = logService.searchLogs(serviceName, level, search, startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Logs retrieved successfully", logs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed log entry including stack trace and contextual metadata")
    public ResponseEntity<ApiResponse<LogEntryDto>> getLogById(@PathVariable UUID id) {
        LogEntryDto log = logService.getLogById(id);
        return ResponseEntity.ok(ApiResponse.ok("Log entry retrieved", log));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Ingest a single application log entry")
    public ResponseEntity<ApiResponse<LogEntryDto>> ingestLog(@Valid @RequestBody IngestLogRequest request) {
        LogEntryDto ingested = logService.ingestLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Log ingested successfully", ingested));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'ENGINEER')")
    @Operation(summary = "Batch ingest multiple application log records")
    public ResponseEntity<ApiResponse<List<LogEntryDto>>> ingestBatchLogs(@Valid @RequestBody BatchIngestLogRequest request) {
        List<LogEntryDto> ingested = logService.ingestBatchLogs(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Batch logs ingested successfully", ingested));
    }

    @GetMapping("/services")
    @Operation(summary = "List all distinct microservices emitting logs")
    public ResponseEntity<ApiResponse<List<String>>> getDistinctServices() {
        List<String> services = logService.getDistinctServiceNames();
        return ResponseEntity.ok(ApiResponse.ok("Services retrieved", services));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get aggregate statistics and level distribution counts")
    public ResponseEntity<ApiResponse<LogStatsDto>> getLogStats() {
        LogStatsDto stats = logService.getLogStats();
        return ResponseEntity.ok(ApiResponse.ok("Log statistics retrieved", stats));
    }
}

