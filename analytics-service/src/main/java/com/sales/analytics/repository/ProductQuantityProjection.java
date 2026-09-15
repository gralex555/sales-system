package com.sales.analytics.repository;

import java.math.BigDecimal;

public interface ProductQuantityProjection {
    Long getTotalQuantity();
    BigDecimal getTotalRevenue();
}
