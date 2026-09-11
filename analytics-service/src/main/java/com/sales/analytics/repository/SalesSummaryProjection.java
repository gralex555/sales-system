package com.sales.analytics.repository;

import java.math.BigDecimal;

public interface SalesSummaryProjection {
    BigDecimal getTotalRevenue();
    Long getOrderCount();
    BigDecimal getAverageOrderValue();
}
