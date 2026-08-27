package com.sales.order.exception;

import com.sales.order.entity.OrderStatus;

public class OrderNotPayableException extends RuntimeException {
    public OrderNotPayableException(Long orderId, OrderStatus status) {
        super(buildMessage(orderId, status));
    }

    private static String buildMessage(Long orderId, OrderStatus status) {
        return switch (status) {
            case PAID -> "Order " + orderId + " is already paid";
            case CANCELLED -> "Order " + orderId + " is cancelled and cannot be paid";
            default -> "Order " + orderId + " cannot be paid in status " + status;
        };
    }
}
