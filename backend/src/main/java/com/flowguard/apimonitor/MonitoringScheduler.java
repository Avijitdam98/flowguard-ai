package com.flowguard.apimonitor;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonitoringScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonitoringScheduler.class);

    private final ApiMonitorRepository monitorRepository;
    private final MonitoringExecutionService executionService;

    // Checks every 15 seconds for monitors due for health checks
    @Scheduled(fixedDelay = 15000)
    public void runScheduledHealthChecks() {
        List<ApiMonitor> activeMonitors = monitorRepository.findByIsActiveTrue();
        if (activeMonitors.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        for (ApiMonitor monitor : activeMonitors) {
            boolean isDue = false;
            if (monitor.getLastCheckedAt() == null) {
                isDue = true;
            } else {
                long elapsedSeconds = now.getEpochSecond() - monitor.getLastCheckedAt().getEpochSecond();
                if (elapsedSeconds >= monitor.getMonitoringIntervalSeconds()) {
                    isDue = true;
                }
            }

            if (isDue) {
                try {
                    executionService.executeProbe(monitor);
                } catch (Exception ex) {
                    log.error("Scheduled probe failed for monitor {}: {}", monitor.getName(), ex.getMessage());
                }
            }
        }
    }
}

