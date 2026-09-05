package com.sales.notification.messaging;

import com.sales.notification.entity.ProcessedEvent;
import com.sales.notification.event.OrderPaidEvent;
import com.sales.notification.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderPaidListener {

    private final ProcessedEventRepository processedEventRepository;

    public OrderPaidListener(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topics.order-paid:order-paid}",
            groupId = "${kafka.consumer.group-id:notification-group}")
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Received OrderPaid event: {}", event);

        int inserted = processedEventRepository.insertIfNotExists(
                event.getEventId(), "ORDER_PAID", LocalDateTime.now());

        if (inserted == 0) {
            log.info("Event {} already processed, skipping", event.getEventId());
            return;
        }

        sendNotification(event);
    }

    private void sendNotification(OrderPaidEvent event) {
        log.info("Notification sent to customer {}: order {} paid, amount {}",
                event.getCustomerId(), event.getOrderId(), event.getTotalAmount());
    }
}
