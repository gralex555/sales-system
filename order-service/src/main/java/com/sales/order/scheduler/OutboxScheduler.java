package com.sales.order.scheduler;

import com.sales.order.messaging.OutboxPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private final OutboxPublisher outboxPublisher;

    public OutboxScheduler(OutboxPublisher outboxPublisher) {
        this.outboxPublisher = outboxPublisher;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-interval-ms:1000}")
    public void publishOutboxEvents() {
        outboxPublisher.publishPendingEvents();
    }

}
