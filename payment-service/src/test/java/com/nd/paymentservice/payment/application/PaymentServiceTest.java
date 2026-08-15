package com.nd.paymentservice.payment.application;

import com.nd.paymentservice.payment.domain.Payment;
import com.nd.paymentservice.payment.domain.PaymentStatus;
import com.nd.paymentservice.payment.messaging.event.OrderCreatedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.persistence.PaymentRepository;
import com.nd.paymentservice.payment.provider.PaymentProvider;
import com.nd.paymentservice.payment.provider.PaymentResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreateSucceededPaymentWhenProviderSucceeds() {
        var event = new OrderCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ONE,
                Instant.now());

        when(processedEventRepository.existsById(event.eventId()))
                .thenReturn(false);

        when(paymentProvider.charge(event.orderId(), event.totalAmount()))
                .thenReturn(PaymentResult.success());

        paymentService.handleOrderCreated(event);

        verify(paymentRepository).save(paymentCaptor.capture());
        verify(processedEventRepository)
                .save(any(ProcessedEvent.class));

        var savedPayment = paymentCaptor.getValue();

        assertEquals(PaymentStatus.SUCCEEDED, savedPayment.getStatus());
        assertEquals(event.orderId(), savedPayment.getOrderId());
        assertEquals(event.totalAmount(), savedPayment.getAmount());
    }

    @Test
    void shouldCreateFailedPaymentWhenProviderFails() {
        var event = new OrderCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ONE,
                Instant.now());

        when(processedEventRepository.existsById(event.eventId()))
                .thenReturn(false);

        when(paymentProvider.charge(event.orderId(), event.totalAmount()))
                .thenReturn(PaymentResult.failed("Funds not enough"));

        paymentService.handleOrderCreated(event);

        verify(paymentRepository).save(paymentCaptor.capture());
        verify(processedEventRepository)
                .save(any(ProcessedEvent.class));

        var savedPayment = paymentCaptor.getValue();

        assertEquals(PaymentStatus.FAILED, savedPayment.getStatus());
        assertEquals(event.orderId(), savedPayment.getOrderId());
        assertEquals(event.totalAmount(), savedPayment.getAmount());
    }
}
