package com.sales.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {
    private String eventId;
    private Long orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    private LocalDateTime paidAt;

    private List<OrderItemData> items;

    public static OrderPaidEvent of(Long orderId, Long customerId,
                                    BigDecimal totalAmount, List<OrderItemData> items) {
        return new OrderPaidEvent(
                UUID.randomUUID().toString(),
                orderId, customerId, totalAmount,
                LocalDateTime.now(), items);
    }
}
