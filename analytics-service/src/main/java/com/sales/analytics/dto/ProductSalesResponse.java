package com.sales.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ProductSalesResponse {
    private Long productId;
    private String productName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}
