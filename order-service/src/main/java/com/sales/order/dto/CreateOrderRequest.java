package com.sales.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CreateOrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;
}
