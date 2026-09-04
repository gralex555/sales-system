package com.sales.notification.messaging;

import com.sales.notification.event.OrderPaidEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPaidListener {

    @KafkaListener(topics = "${kafka.topics.order-paid:order-paid}",
            groupId = "${kafka.consumer.group-id:notification-group}")
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Received OrderPaid event: {}", event);

        // имитация отправки уведомления
        sendNotification(event);
    }

    private void sendNotification(OrderPaidEvent event) {
        log.info("Notification sent to customer {}: order {} paid, amount {}",
                event.getCustomerId(), event.getOrderId(), event.getTotalAmount());
    }
}
