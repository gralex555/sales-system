package com.sales.payment.exception;

import com.sales.payment.entity.PaymentStatus;

public class PaymentRefundNotAllowedException extends RuntimeException {
    public PaymentRefundNotAllowedException(Long paymentId, PaymentStatus status) {
        super("Cannot refund payment " + paymentId + " in status " + status);
    }
}
