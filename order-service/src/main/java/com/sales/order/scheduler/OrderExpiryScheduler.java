package com.sales.order.scheduler;

import com.sales.order.entity.Order;
import com.sales.order.entity.OrderStatus;
import com.sales.order.repository.OrderRepository;
import com.sales.order.service.OrderExpiryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderExpiryScheduler {
    private final OrderRepository orderRepository;
    private final OrderExpiryService expiryService;
    private final int batchSize;

    public OrderExpiryScheduler(OrderRepository orderRepository,
                                OrderExpiryService expiryService,
                                @Value("${order.expiry-batch-size:100}") int batchSize) {
        this.orderRepository = orderRepository;
        this.expiryService = expiryService;
        this.batchSize = batchSize;
    }


    @Scheduled(fixedDelayString = "${order.expiry-check-interval-ms:60000}")
    public void cancelExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();

        List<Order> expired = orderRepository.findExpiredOrders(
                OrderStatus.CREATED, now, PageRequest.of(0, batchSize));

        if (expired.isEmpty()) {
            return;
        }

        log.info("Found {} expired orders to cancel", expired.size());

        for (Order order : expired) {
            if (expiryService.tryCancelExpiredOrder(order, now)) {
                log.info("Order {} expired, releasing reservations", order.getId());
                expiryService.releaseReservations(order);
            }
        }
    }

}
