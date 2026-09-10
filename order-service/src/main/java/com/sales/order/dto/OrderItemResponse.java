package com.sales.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponse {
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
}
