package com.sales.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CreatePaymentRequest {
    private Long orderId;
    private BigDecimal amount;
}
