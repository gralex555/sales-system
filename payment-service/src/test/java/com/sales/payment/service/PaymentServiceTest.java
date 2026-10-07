package com.sales.payment.service;

import com.sales.payment.dto.PaymentResponse;
import com.sales.payment.entity.Payment;
import com.sales.payment.entity.PaymentStatus;
import com.sales.payment.exception.PaymentNotFoundException;
import com.sales.payment.exception.PaymentRefundNotAllowedException;
import com.sales.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    
    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, BigDecimal.valueOf(1_000_000));
    }

    private Payment paymentWithStatus(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(10L);
        payment.setAmount(BigDecimal.valueOf(900));
        payment.setStatus(status);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }

    @Test
    void shouldRefundCompletedPayment() {
        Payment payment = paymentWithStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        PaymentResponse response = paymentService.refundPayment(1L);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void shouldNotSaveWhenAlreadyRefunded() {
        // given
        Payment payment = paymentWithStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // when
        PaymentResponse response = paymentService.refundPayment(1L);

        // then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository, never()).save(any(Payment.class));

    }

    @Test
    void shouldNotSaveWhenPaymentFailed() {
        // given
        Payment payment = paymentWithStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // when
        PaymentResponse response = paymentService.refundPayment(1L);

        // then
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository, never()).save(any(Payment.class));

    }

    @Test
    void shouldThrowWhenPaymentPending() {
        // given
        Payment payment = paymentWithStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L))
                .isInstanceOf(PaymentRefundNotAllowedException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refundPayment(999L))
                .isInstanceOf(PaymentNotFoundException.class);

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
