package com.sales.order.exception;

public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String reason) {
        super("Payment declined: " + reason);
    }
}
