package com.flowguard.apimonitor;

import com.flowguard.apimonitor.dto.MonitoringResultDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MonitoringExecutionService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringExecutionService.class);

    private final SsrfProtectionValidator ssrfValidator;
    private final ApiMonitorRepository monitorRepository;
    private final MonitoringResultRepository resultRepository;

    @Transactional
    public MonitoringResultDto executeProbe(ApiMonitor monitor) {
        String fullUrl = monitor.getFullUrl();
        log.info("Executing health probe on monitor '{}' [{}] {}", monitor.getName(), monitor.getHttpMethod(), fullUrl);

        // 1. SSRF Validation
        ssrfValidator.validateUrl(fullUrl);

        int statusCode = 0;
        int latencyMs = 0;
        boolean success = false;
        String errorMessage = null;
        String responseBody = null;

        long startTime = System.currentTimeMillis();

        try {
            URL url = URI.create(fullUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(monitor.getHttpMethod().name());
            conn.setConnectTimeout(monitor.getTimeoutMs());
            conn.setReadTimeout(monitor.getTimeoutMs());
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "FlowGuard-Monitor-Agent/1.0");

            conn.connect();

            statusCode = conn.getResponseCode();
            long endTime = System.currentTimeMillis();
            latencyMs = (int) (endTime - startTime);

            try (InputStream is = (statusCode >= 400) ? conn.getErrorStream() : conn.getInputStream()) {
                if (is != null) {
                    byte[] bytes = is.readNBytes(512); // Sample first 512 bytes
                    responseBody = new String(bytes, StandardCharsets.UTF_8);
                }
            }

            success = (statusCode == monitor.getExpectedHttpStatus());
            if (!success) {
                errorMessage = "Expected HTTP " + monitor.getExpectedHttpStatus() + " but received HTTP " + statusCode;
            }

        } catch (Exception ex) {
            long endTime = System.currentTimeMillis();
            latencyMs = (int) Math.max(1, endTime - startTime);
            statusCode = 504;
            success = false;
            errorMessage = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            log.warn("Probe execution error for '{}': {}", monitor.getName(), errorMessage);
        }

        // 2. Evaluate Health Status
        MonitorStatus newStatus;
        if (success) {
            newStatus = (latencyMs > monitor.getAlertThresholdMs()) ? MonitorStatus.DEGRADED : MonitorStatus.HEALTHY;
        } else {
            newStatus = (statusCode >= 500) ? MonitorStatus.DOWN : MonitorStatus.DEGRADED;
        }

        // 3. Update Monitor
        monitor.setLastCheckedAt(Instant.now());
        monitor.setCurrentStatus(newStatus);
        monitorRepository.save(monitor);

        // 4. Save Result
        MonitoringResult result = MonitoringResult.builder()
                .monitorId(monitor.getId())
                .statusCode(statusCode)
                .responseTimeMs(latencyMs)
                .isSuccessful(success)
                .errorMessage(errorMessage)
                .responseBodySample(responseBody)
                .checkedAt(Instant.now())
                .build();

        MonitoringResult saved = resultRepository.save(result);
        return MonitoringResultDto.fromEntity(saved);
    }
}

