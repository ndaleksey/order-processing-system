package com.nd.orderservice.order.infrastructure.kafka;

import com.nd.orderservice.order.application.OrderPaymentResultService;
import com.nd.orderservice.order.application.event.PaymentFailedEvent;
import com.nd.orderservice.order.application.event.PaymentSucceededEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class PaymentResultConsumerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderPaymentResultService orderPaymentResultService;

    private PaymentResultConsumer paymentResultConsumer;

    @BeforeEach
    void setUp() {
        paymentResultConsumer = new PaymentResultConsumer(objectMapper, orderPaymentResultService);
    }

    @Test
    void shouldHandlePaymentSucceededEvent() {
        var event = PaymentSucceededEvent.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now());

        var payload = objectMapper.writeValueAsString(event);

        paymentResultConsumer.consume(payload);

        verify(orderPaymentResultService).handlePaymentSucceededEvent(event);
        verify(orderPaymentResultService, never()).handlePaymentFailedEvent(any());
    }

    @Test
    void shouldHandlePaymentFailedEvent() {
        var event = PaymentFailedEvent.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Insufficient funds",
                Instant.now());

        var payload = objectMapper.writeValueAsString(event);

        paymentResultConsumer.consume(payload);

        verify(orderPaymentResultService).handlePaymentFailedEvent(event);
        verify(orderPaymentResultService, never()).handlePaymentSucceededEvent(any());
    }
}