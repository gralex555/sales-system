package com.sales.order.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentInfo {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String status;          // COMPLETED / FAILED / REFUNDED
    private String failureReason;
}
