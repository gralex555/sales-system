package com.sales.order.service;

import com.sales.order.client.PaymentServiceClient;
import com.sales.order.client.ProductServiceClient;
import com.sales.order.client.dto.ProductInfo;
import com.sales.order.dto.CreateOrderRequest;
import com.sales.order.dto.OrderItemRequest;
import com.sales.order.dto.OrderResponse;
import com.sales.order.entity.Order;
import com.sales.order.entity.OrderStatus;
import com.sales.order.exception.OrderNotFoundException;
import com.sales.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private OutboxService outboxService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                productServiceClient,
                paymentServiceClient,
                outboxService,
                48,                  // reservationHours
                "order-paid");       // orderPaidTopic
    }

    @Test
    void shouldCalculateTotalAmountFromItems() {
        // arrange
        ProductInfo product = new ProductInfo();
        product.setId(1L);
        product.setPrice(BigDecimal.valueOf(450));

        when(productServiceClient.getProduct(anyLong())).thenReturn(product);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(AdditionalAnswers.returnsFirstArg());

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(3);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(42L);
        request.setItems(List.of(item));

        // act
        OrderResponse response = orderService.createOrder(request);

        // assert
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1350));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        Long nonExistentId = 999L;
        when(orderRepository.findByIdWithItems(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(nonExistentId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
