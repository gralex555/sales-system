package com.sales.order.entity;

public enum OrderStatus {
    CREATED,     // создан, товар зарезервирован, ждём оплату
    PAID,        // оплачен, готов к сборке
    CANCELLED    // отменён (нет товара / не оплатил вовремя / отмена клиентом)
}
