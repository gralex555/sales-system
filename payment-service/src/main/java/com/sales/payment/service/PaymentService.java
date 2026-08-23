package com.sales.payment.service;

import com.sales.payment.dto.CreatePaymentRequest;
import com.sales.payment.dto.PaymentResponse;
import com.sales.payment.entity.Payment;
import com.sales.payment.entity.PaymentStatus;
import com.sales.payment.exception.PaymentDeclinedException;
import com.sales.payment.exception.PaymentNotFoundException;
import com.sales.payment.exception.PaymentRefundNotAllowedException;
import com.sales.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BigDecimal paymentLimit;

    public PaymentService(PaymentRepository paymentRepository,
                          @Value("${payment.transaction-limit:1000000}") BigDecimal paymentLimit) {
        this.paymentRepository = paymentRepository;
        this.paymentLimit = paymentLimit;
    }

    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request) {
        log.info("Processing payment for order {} amount {}", request.getOrderId(), request.getAmount());

        // 1. фиксируем намерение
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // 2. обращаемся к "шлюзу"
        try {
            callPaymentGateway(request.getAmount());
            payment.setStatus(PaymentStatus.COMPLETED);
            log.info("Payment {} completed for order {}", payment.getId(), payment.getOrderId());
        } catch (PaymentDeclinedException e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            log.warn("Payment {} declined for order {}: {}",
                    payment.getId(), payment.getOrderId(), e.getMessage());
        }

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    private PaymentResponse mapToResponse(Payment payment ) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    private void callPaymentGateway(BigDecimal amount) {
        // ИМИТАЦИЯ платёжного шлюза: отказ при превышении лимита
        if (amount.compareTo(paymentLimit) > 0) {
            throw new PaymentDeclinedException(
                    "Amount " + amount + " exceeds transaction limit " + paymentLimit);
        }
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment {} is already refunded, nothing to do", id);
            return mapToResponse(payment);
        }

        // не прошёл
        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Payment {} was failed, nothing to refund", id);
            return mapToResponse(payment);
        }

        // состояние неизвестно
        if (payment.getStatus() == PaymentStatus.PENDING) {
            throw new PaymentRefundNotAllowedException(id, payment.getStatus());
        }

        // COMPLETED — возвращаем
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment {} refunded for order {}", saved.getId(), saved.getOrderId());
        return mapToResponse(saved);
    }
}
