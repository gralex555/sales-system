package com.sales.analytics.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CustomerSalesResponse {

    private Long customerId;
    private Long orderCount;
    private BigDecimal totalRevenue;
}

