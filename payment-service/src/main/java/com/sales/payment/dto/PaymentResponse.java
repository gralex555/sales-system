package com.sales.payment.dto;

import com.sales.payment.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponse {

    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private PaymentStatus status;

    private String failureReason;

    private LocalDateTime createdAt;
}
