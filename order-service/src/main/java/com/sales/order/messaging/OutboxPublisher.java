package com.sales.order.messaging;

import com.sales.order.entity.OutboxEvent;
import com.sales.order.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final int batchSize;

    public OutboxPublisher(OutboxEventRepository outboxRepository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${outbox.batch-size:100}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.batchSize = batchSize;
    }

    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findUnpublishedForUpdate(batchSize);

        if (events.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        event.getTopic(),
                        String.valueOf(event.getAggregateId()),
                        event.getPayload()).get();
                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                log.info("Outbox event {} published: type={}, aggregateId={}",
                        event.getId(), event.getEventType(), event.getAggregateId());

            } catch (Exception e) {
                event.setAttempts(event.getAttempts() + 1);
                log.error("Failed to publish outbox event {} (attempt {}): {}",
                        event.getId(), event.getAttempts(), e.getMessage());
            }
        }

        outboxRepository.saveAll(events);
    }
}
