package com.sales.order.messaging;

import com.sales.order.event.OrderPaidEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderPaidTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${kafka.topics.order-paid:order-paid}") String orderPaidTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderPaidTopic = orderPaidTopic;
    }

    public void publishOrderPaid(OrderPaidEvent event) {
        log.info("Publishing OrderPaid event {} for order {}", event.getEventId(), event.getOrderId());
        kafkaTemplate.send(orderPaidTopic, String.valueOf(event.getOrderId()), event);
    }
}
