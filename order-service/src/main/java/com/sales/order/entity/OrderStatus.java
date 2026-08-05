package com.sales.order.entity;

public enum OrderStatus {
    CREATED,      // заказ создан, ещё ничего не проверено
    RESERVED,     // товар зарезервирован на складе
    PAID,         // оплата прошла
    CONFIRMED,    // заказ подтверждён (финальный успех)
    CANCELLED     // заказ отменён
}
