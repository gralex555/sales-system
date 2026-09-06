package com.sales.order.service;

import com.sales.order.client.PaymentServiceClient;
import com.sales.order.client.ProductServiceClient;
import com.sales.order.client.dto.PaymentInfo;
import com.sales.order.client.dto.ProductInfo;
import com.sales.order.dto.CreateOrderRequest;
import com.sales.order.dto.OrderItemRequest;
import com.sales.order.dto.OrderItemResponse;
import com.sales.order.dto.OrderResponse;
import com.sales.order.entity.Order;
import com.sales.order.entity.OrderItem;
import com.sales.order.entity.OrderStatus;
import com.sales.order.event.OrderPaidEvent;
import com.sales.order.exception.OrderNotFoundException;
import com.sales.order.exception.OrderNotPayableException;
import com.sales.order.exception.PaymentDeclinedException;
import com.sales.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OutboxService outboxService;
    private final int reservationHours;
    private final String orderPaidTopic;

    public OrderService(OrderRepository orderRepository,
                        ProductServiceClient productServiceClient,
                        PaymentServiceClient paymentServiceClient,
                        OutboxService outboxService,
                        @Value("${order.reservation-hours:48}") int reservationHours,
                        @Value("${kafka.topics.order-paid:order-paid}") String orderPaidTopic) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.outboxService = outboxService;
        this.reservationHours = reservationHours;
        this.orderPaidTopic = orderPaidTopic;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer {}", request.getCustomerId());
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;    // накопитель суммы
        List<ReservedItem> reserved = new ArrayList<>();     // память о шагах. Список выполненных резервов

        try {
            for (OrderItemRequest itemRequest : request.getItems()) {

                ProductInfo product = productServiceClient.getProduct(itemRequest.getProductId());
                BigDecimal price = product.getPrice();

                productServiceClient.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());
                reserved.add(new ReservedItem(itemRequest.getProductId(), itemRequest.getQuantity()));

                OrderItem item = new OrderItem();
                item.setProductId(itemRequest.getProductId());
                item.setQuantity(itemRequest.getQuantity());
                item.setPrice(price);
                item.setOrder(order);
                order.getItems().add(item);

                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

            }

            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.CREATED);
            order.setReservedUntil(LocalDateTime.now().plusHours(reservationHours));

            Order savedOrder = orderRepository.save(order);
            log.info("Order {} created, reserved until {}", savedOrder.getId(), savedOrder.getReservedUntil());
            return mapToResponse(savedOrder);

        } catch (Exception e) {
            log.warn("Order creation failed, compensating {} reservations", reserved.size());
            compensateReservations(reserved);
            throw e;
        }
    }

    @Transactional
    public OrderResponse payOrder(Long orderId) {
        log.info("Processing payment for order {}", orderId);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new OrderNotPayableException(orderId, order.getStatus());
        }

        PaymentInfo payment = paymentServiceClient.processPayment(orderId, order.getTotalAmount());

        if ("FAILED".equals(payment.getStatus())) {
            log.warn("Payment declined for order {}: {}", orderId, payment.getFailureReason());
            throw new PaymentDeclinedException(payment.getFailureReason());
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        OrderPaidEvent event = OrderPaidEvent.of(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount());

        outboxService.save(savedOrder.getId(), "ORDER_PAID", orderPaidTopic, event);

        log.info("Order {} paid successfully", orderId);
        return mapToResponse(savedOrder);
    }

    private void compensatePayment(Long paymentId) {
        try {
            paymentServiceClient.refundPayment(paymentId);
            log.info("Compensated payment {}", paymentId);
        } catch (Exception e) {
            log.error("FAILED to compensate payment {}. Manual intervention required.", paymentId, e);
        }
    }

    private void compensateOrder(Order order) {
        try {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} cancelled", order.getId());
        } catch (Exception e) {
            log.error("FAILED to cancel order {}", order.getId(), e);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setReservedUntil(order.getReservedUntil());

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setProductId(item.getProductId());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());

            itemResponses.add(itemResponse);
        }
        response.setItems(itemResponses);

        return response;
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToResponse(order);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private void compensateReservations(List<ReservedItem> reserved) {

        for (int i = reserved.size() - 1; i >= 0; i--) {
            ReservedItem item = reserved.get(i);
            try {
                productServiceClient.releaseStock(item.productId(), item.quantity());
                log.info("Compensated reservation: product {} x{}",
                        item.productId(), item.quantity());
            } catch (Exception e) {
                log.error("FAILED to compensate reservation: product {} x{}. Manual intervention required.",
                        item.productId(), item.quantity(), e);
            }
        }
    }
}
