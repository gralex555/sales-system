package com.sales.product.exception;

public class CertificateNotFoundException extends RuntimeException {
    public CertificateNotFoundException(Long productId) {
        super("У товара нет сертификата: " + productId);
    }
}
