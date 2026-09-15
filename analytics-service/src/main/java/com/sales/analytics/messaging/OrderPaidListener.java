package com.sales.analytics.messaging;

import com.sales.analytics.event.OrderItemData;
import com.sales.analytics.event.OrderPaidEvent;
import com.sales.analytics.entity.SalesItem;
import com.sales.analytics.entity.SalesOrder;
import com.sales.analytics.repository.ProcessedEventRepository;
import com.sales.analytics.repository.SalesOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderPaidListener {

    private final SalesOrderRepository salesOrderRepository;
    private final ProcessedEventRepository processedEventRepository;

    public OrderPaidListener(SalesOrderRepository salesOrderRepository,
                             ProcessedEventRepository processedEventRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topics.order-paid:order-paid}",
            groupId = "${kafka.consumer.group-id:analytics-group}")
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Received OrderPaid event for order {}", event.getOrderId());

        int inserted = processedEventRepository.insertIfNotExists(
                event.getEventId(), "ORDER_PAID", LocalDateTime.now());

        if (inserted == 0) {
            log.info("Event {} already processed, skipping", event.getEventId());
            return;
        }
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setOrderId(event.getOrderId());
        salesOrder.setCustomerId(event.getCustomerId());
        salesOrder.setTotalAmount(event.getTotalAmount());
        salesOrder.setPaidAt(event.getPaidAt());

        if (event.getItems() != null) {
            for (OrderItemData itemData : event.getItems()) {
                SalesItem item = new SalesItem();
                item.setProductId(itemData.getProductId());
                item.setProductName(itemData.getProductName());
                item.setQuantity(itemData.getQuantity());
                item.setPrice(itemData.getPrice());
                item.setSalesOrder(salesOrder);
                salesOrder.getItems().add(item);
            }
        }

        salesOrderRepository.save(salesOrder);
        log.info("Sale recorded: order {}, customer {}, amount {}, {} items",
                event.getOrderId(), event.getCustomerId(),
                event.getTotalAmount(), salesOrder.getItems().size());
    }
}
