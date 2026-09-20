package com.nd.paymentservice.payment.application;

import com.nd.paymentservice.payment.application.command.CompensatePaymentCommand;
import com.nd.paymentservice.payment.domain.Payment;
import com.nd.paymentservice.payment.domain.PaymentStatus;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEvent;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEventFactory;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEventRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */

@ExtendWith(MockitoExtension.class)
class PaymentCompensationServiceTest {
    @Captor
    private ArgumentCaptor<ProcessedEvent> processedEventCaptor;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private PaymentCompensationService service;

    @Test
    void shouldCompensatePaymentAndCreateOutboxEvent() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(100);

        var command = new CompensatePaymentCommand(eventId, orderId);

        var payment = Payment.create(orderId, amount);
        payment.succeed();

        var outboxEvent = OutboxEvent.createCompensated(
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                "{}"
        );

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false);

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        when(paymentProvider.refund(orderId, amount))
                .thenReturn(PaymentResult.success());

        when(outboxEventFactory.createPaymentCompensatedEvent(payment))
                .thenReturn(outboxEvent);

        service.compensate(command);

        assertEquals(PaymentStatus.COMPENSATED, payment.getStatus());

        verify(paymentProvider).refund(orderId, amount);
        verify(outboxEventRepository).save(outboxEvent);
        verify(processedEventRepository).save(processedEventCaptor.capture());

        assertEquals(eventId, processedEventCaptor.getValue().getEventId());
    }

    @Test
    void shouldIgnoreAlreadyProcessedCompensation() {
        var eventId = UUID.randomUUID();
        var command = new CompensatePaymentCommand(
                eventId,
                UUID.randomUUID()
        );

        when(processedEventRepository.existsById(eventId))
                .thenReturn(true);

        service.compensate(command);

        verify(processedEventRepository).existsById(eventId);

        verifyNoInteractions(
                paymentRepository,
                paymentProvider,
                outboxEventFactory,
                outboxEventRepository
        );

        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void shouldNotCompleteCompensationWhenRefundFails() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(100);

        var command = new CompensatePaymentCommand(eventId, orderId);

        var payment = Payment.create(orderId, amount);
        payment.succeed();

        when(processedEventRepository.existsById(eventId))
                .thenReturn(false);

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        when(paymentProvider.refund(orderId, amount))
                .thenReturn(PaymentResult.failed("Can't refund"));

        assertThrows(RuntimeException.class, () -> service.compensate(command));

        verify(paymentProvider).refund(orderId, amount);

        verifyNoInteractions(outboxEventFactory, outboxEventRepository);
        verify(processedEventRepository, never()).save(any());
    }
}