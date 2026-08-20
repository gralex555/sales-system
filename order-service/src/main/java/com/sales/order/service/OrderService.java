package com.sales.order.service;

import com.sales.order.client.ProductServiceClient;
import com.sales.order.client.dto.ProductInfo;
import com.sales.order.dto.CreateOrderRequest;
import com.sales.order.dto.OrderItemRequest;
import com.sales.order.dto.OrderItemResponse;
import com.sales.order.dto.OrderResponse;
import com.sales.order.entity.Order;
import com.sales.order.entity.OrderItem;
import com.sales.order.entity.OrderStatus;
import com.sales.order.exception.OrderNotFoundException;
import com.sales.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public OrderService(OrderRepository orderRepository,
                        ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer {}", request.getCustomerId());
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;    // накопитель суммы

        for (OrderItemRequest itemRequest : request.getItems()) {

            ProductInfo product = productServiceClient.getProduct(itemRequest.getProductId());
            BigDecimal price = product.getPrice();

            productServiceClient.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());

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
        order.setStatus(OrderStatus.RESERVED);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id {} and status {}", savedOrder.getId(), savedOrder.getStatus());
        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());

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
}
