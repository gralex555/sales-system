package com.sales.order.service;

import com.sales.order.dto.CreateOrderRequest;
import com.sales.order.dto.OrderItemRequest;
import com.sales.order.dto.OrderItemResponse;
import com.sales.order.dto.OrderResponse;
import com.sales.order.entity.Order;
import com.sales.order.entity.OrderItem;
import com.sales.order.entity.OrderStatus;
import com.sales.order.exception.OrderNotFoundException;
import com.sales.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;    // накопитель суммы

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());

            BigDecimal price = getPriceForProduct(itemRequest.getProductId());  // цена (заглушка пока)
            item.setPrice(price);

            item.setOrder(order);
            order.getItems().add(item);

            // накопить сумму: цена × количество
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    private BigDecimal getPriceForProduct(Long productId) {
        // ЗАГЛУШКА: пока product-service нет, возвращаем условную цену
        // На Этапе 2 заменим реальным запросом в product-service
        return BigDecimal.valueOf(100);
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

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllWithItems().stream()
                .map(this::mapToResponse)
                .toList();
    }
}
