package com.sales.analytics.repository;

import java.math.BigDecimal;

public interface ProductSalesProjection {

    Long getProductId();
    String getProductName();
    Long getTotalQuantity();
    BigDecimal getTotalRevenue();
}
