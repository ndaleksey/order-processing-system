package com.nd.paymentservice.payment.application;

import com.nd.paymentservice.payment.application.command.CompensatePaymentCommand;
import com.nd.paymentservice.payment.domain.Payment;
import com.nd.paymentservice.payment.domain.PaymentStatus;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEventRepository;
import com.nd.paymentservice.payment.persistence.PaymentRepository;
import com.nd.paymentservice.payment.provider.PaymentProvider;
import com.nd.paymentservice.payment.provider.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
class PaymentCompensationServiceRollbackIntegrationTest {

    @Autowired
    private PaymentCompensationService compensationService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @MockitoBean
    private PaymentProvider paymentProvider;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        processedEventRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @Test
    void shouldRollbackCompensationWhenRefundFails() {
        var orderId = UUID.randomUUID();
        var eventId = UUID.randomUUID();
        var amount = BigDecimal.valueOf(100);

        var payment = Payment.create(orderId, amount);
        payment.succeed();

        var savedPayment = paymentRepository.save(payment);

        when(paymentProvider.refund(orderId, amount))
                .thenReturn(PaymentResult.failed("Can't refund"));

        var command = new CompensatePaymentCommand(eventId, orderId);

        assertThrows(
                RuntimeException.class,
                () -> compensationService.compensate(command)
        );

        var paymentAfterRollback = paymentRepository.findById(savedPayment.getId())
                .orElseThrow();

        assertEquals(
                PaymentStatus.SUCCEEDED,
                paymentAfterRollback.getStatus()
        );

        assertEquals(0, outboxEventRepository.count());
        assertFalse(processedEventRepository.existsById(eventId));
    }
}