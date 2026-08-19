package com.sales.order.exception;

public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(Long productId) {
        super("Product not found: " + productId);
    }
}
