package com.sales.analytics.repository;

import java.math.BigDecimal;

public interface CustomerSalesProjection {
    Long getCustomerId();
    Long getOrderCount();
    BigDecimal getTotalRevenue();
}
