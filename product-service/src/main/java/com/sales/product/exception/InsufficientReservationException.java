package com.sales.product.exception;

public class InsufficientReservationException extends RuntimeException {

    public InsufficientReservationException(Long productId, Integer quantity) {
        super("Cannot release " + quantity + " units of product " + productId
                + ": not enough reserved");
    }
}
