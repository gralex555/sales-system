package com.sales.product.entity;

public enum ReservationResult {
    SUCCESS,           // зарезервировано
    INSUFFICIENT_STOCK // не хватило товара
    // NEEDS_PRODUCTION — добавим при расширении, ядро не трогая
}
