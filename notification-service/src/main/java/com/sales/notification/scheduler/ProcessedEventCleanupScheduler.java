package com.sales.notification.scheduler;

import com.sales.notification.service.ProcessedEventCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessedEventCleanupScheduler {

    private final ProcessedEventCleanupService cleanupService;

    public ProcessedEventCleanupScheduler(ProcessedEventCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(cron = "${processed-event.cleanup.cron:0 30 3 * * *}")
    public void cleanupOldEvents() {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = cleanupService.deleteBatch();
            totalDeleted += deleted;
        } while (deleted > 0);

        if (totalDeleted > 0) {
            log.info("Processed events cleanup: deleted {} old records", totalDeleted);
        }
    }
}
