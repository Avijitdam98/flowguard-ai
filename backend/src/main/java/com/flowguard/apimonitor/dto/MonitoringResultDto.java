package com.flowguard.apimonitor.dto;

import com.flowguard.apimonitor.MonitoringResult;
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
public class MonitoringResultDto {
    private UUID id;
    private UUID monitorId;
    private Integer statusCode;
    private Integer responseTimeMs;
    private Boolean isSuccessful;
    private String errorMessage;
    private String responseHeaders;
    private String responseBodySample;
    private Instant checkedAt;

    public static MonitoringResultDto fromEntity(MonitoringResult result) {
        if (result == null) return null;
        return MonitoringResultDto.builder()
                .id(result.getId())
                .monitorId(result.getMonitorId())
                .statusCode(result.getStatusCode())
                .responseTimeMs(result.getResponseTimeMs())
                .isSuccessful(result.getIsSuccessful())
                .errorMessage(result.getErrorMessage())
                .responseHeaders(result.getResponseHeaders())
                .responseBodySample(result.getResponseBodySample())
                .checkedAt(result.getCheckedAt())
                .build();
    }
}

