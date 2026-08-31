package com.sales.order.client;

import com.sales.order.client.dto.CreatePaymentRequest;
import com.sales.order.client.dto.PaymentInfo;
import com.sales.order.exception.PaymentServiceUnavailableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class PaymentServiceClient {
    private final RestClient restClient;

    public PaymentServiceClient(RestClient paymentRestClient) {
        this.restClient = paymentRestClient;
    }

    public PaymentInfo processPayment(Long orderId, BigDecimal amount) {
        try {
            return restClient.post()
                    .uri("/api/v1/payments")
                    .body(new CreatePaymentRequest(orderId, amount))
                    .retrieve()
                    .body(PaymentInfo.class);
        } catch (ResourceAccessException ex) {
            throw new PaymentServiceUnavailableException("Payment service is unavailable", ex);
        }
    } // метод проведения платежа

    public void refundPayment(Long paymentId) {
        restClient.post()
                .uri("/api/v1/payments/{id}/refund", paymentId)
                .retrieve()
                .toBodilessEntity();
    } // метод возврата (компенсация)
}
