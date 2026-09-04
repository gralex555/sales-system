package com.sales.notification.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderPaidEvent {
    private String eventId;
    private Long orderId;
    private Long customerId;
    private BigDecimal totalAmount;
    private LocalDateTime paidAt;
}
