package com.sales.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SalesSummaryResponse {

    private LocalDateTime from;
    private LocalDateTime to;
    private BigDecimal totalRevenue;
    private Long orderCount;
    private BigDecimal averageOrderValue;
}
