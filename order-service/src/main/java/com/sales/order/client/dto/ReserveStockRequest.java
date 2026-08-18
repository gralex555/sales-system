package com.sales.order.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReserveStockRequest {
    private Integer quantity;
}
