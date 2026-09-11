package com.sales.analytics.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrderItemData {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
