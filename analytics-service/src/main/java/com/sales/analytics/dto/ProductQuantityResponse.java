package com.sales.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ProductQuantityResponse {
    private Long productId;
    private LocalDateTime from;
    private LocalDateTime to;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
