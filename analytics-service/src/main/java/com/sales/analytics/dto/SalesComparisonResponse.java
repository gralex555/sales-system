package com.sales.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class SalesComparisonResponse {
    private SalesSummaryResponse current;
    private SalesSummaryResponse previous;
    private BigDecimal revenueChangePercent;
    private BigDecimal orderCountChangePercent;
}
