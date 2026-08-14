package com.sales.order.service;

import com.sales.order.dto.CreateOrderRequest;
import com.sales.order.dto.OrderItemRequest;
import com.sales.order.dto.OrderResponse;
import com.sales.order.entity.Order;
import com.sales.order.entity.OrderStatus;
import com.sales.order.exception.OrderNotFoundException;
import com.sales.order.repository.OrderRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCalculateTotalAmountFromItems() {
        // arrange
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(42L);

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(100);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(50);

        request.setItems(List.of(item1, item2));

        // мок: save вернёт тот же заказ, что ему передали
        when(orderRepository.save(any(Order.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        // act
        OrderResponse response = orderService.createOrder(request);

        // assert
        // цена-заглушка 100: (100 × 100) + (50 × 100) = 15000
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(15000));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getItems()).hasSize(2);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        Long nonExistentId = 999L;

        when(orderRepository.findByIdWithItems(nonExistentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(nonExistentId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("999");
    }
}
