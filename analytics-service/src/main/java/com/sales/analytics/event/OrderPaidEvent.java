package com.sales.analytics.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private List<OrderItemData> items;
}
