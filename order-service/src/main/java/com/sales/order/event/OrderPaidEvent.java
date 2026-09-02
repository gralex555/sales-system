package com.sales.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public static OrderPaidEvent of(Long orderId, Long customerId, BigDecimal totalAmount) {
        return new OrderPaidEvent(
                UUID.randomUUID().toString(),
                orderId,
                customerId,
                totalAmount,
                LocalDateTime.now());
    }
}
