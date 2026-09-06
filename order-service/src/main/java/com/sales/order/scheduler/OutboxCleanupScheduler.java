package com.sales.order.scheduler;

import com.sales.order.service.OutboxCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OutboxCleanupScheduler {

    private final OutboxCleanupService cleanupService;

    public OutboxCleanupScheduler(OutboxCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(cron = "${outbox.cleanup.cron:0 0 3 * * *}")  // Cron-выражение 0 0 3 * * * означает «каждый день в 3:00».
    public void cleanupOldEvents() {
        log.info("Cleanup started");     // временно
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = cleanupService.deleteBatch();
            totalDeleted += deleted;
        } while (deleted > 0);

        if (totalDeleted > 0) {
            log.info("Outbox cleanup: deleted {} published events", totalDeleted);
        }
    }
}
