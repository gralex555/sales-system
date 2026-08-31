package com.sales.order.service;

import com.sales.order.client.ProductServiceClient;
import com.sales.order.entity.Order;
import com.sales.order.entity.OrderItem;
import com.sales.order.entity.OrderStatus;
import com.sales.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class OrderExpiryService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public OrderExpiryService(OrderRepository orderRepository,
                              ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }

    @Transactional
    public boolean tryCancelExpiredOrder(Order order, LocalDateTime now) {
        int updated = orderRepository.cancelIfStillCreated(
                order.getId(), OrderStatus.CANCELLED, OrderStatus.CREATED, now);

        if (updated == 0) {
            log.debug("Order {} already processed by another instance, skipping", order.getId());
            return false;
        }
        return true;
    }

    public void releaseReservations(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                productServiceClient.releaseStock(item.getProductId(), item.getQuantity());
                log.info("Released reservation for order {}: product {} x{}",
                        order.getId(), item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("FAILED to release reservation for order {}: product {} x{}. Manual intervention required.",
                        order.getId(), item.getProductId(), item.getQuantity(), e);
            }
        }
    }

}
