package com.flowguard.logmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStatsDto {
    private long totalLogs;
    private long errorCount;
    private long warningCount;
    private long infoCount;
    private Map<String, Long> countByLevel;
}

