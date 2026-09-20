package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.exception.OrderNotFoundException;
import com.nd.orderservice.order.messaging.event.PaymentFailedEvent;
import com.nd.orderservice.order.messaging.event.PaymentSucceededEvent;
import com.nd.orderservice.order.domain.Order;
import com.nd.orderservice.order.domain.OrderStatus;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEvent;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEventRepository;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEvent;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventFactory;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@ExtendWith(MockitoExtension.class)
class OrderPaymentResultServiceTest {

    @Captor
    ArgumentCaptor<ProcessedEvent> captorProcessedEvent;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OutboxEventFactory outboxEventFactory;

    @InjectMocks
    private OrderPaymentResultService orderPaymentResultService;

    @Test
    void shouldMarkOrderPaidWhenPaymentSucceeded() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var event = PaymentSucceededEvent.create(eventId, orderId, paymentId, Instant.now());
        var order = Order.create(UUID.randomUUID());

        var outboxEvent = mock(OutboxEvent.class);

        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(orderRepository.findById(event.orderId())).thenReturn(Optional.of(order));
        when(outboxEventFactory.createInventoryReservationRequested(order))
                .thenReturn(outboxEvent);

        orderPaymentResultService.handlePaymentSucceededEvent(event);

        assertEquals(OrderStatus.PAID, order.getStatus());

        verify(processedEventRepository).save(captorProcessedEvent.capture());

        assertEquals(eventId, captorProcessedEvent.getValue().getEventId());

        verify(outboxEventFactory).createInventoryReservationRequested(order);
        verify(outboxEventRepository).save(outboxEvent);
    }

    @Test
    void shouldCancelOrderWhenPaymentFailed() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var event = PaymentFailedEvent.create(eventId, orderId, paymentId, "Insufficient funds", Instant.now());
        var order = Order.create(UUID.randomUUID());

        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(orderRepository.findById(event.orderId())).thenReturn(Optional.of(order));

        orderPaymentResultService.handlePaymentFailedEvent(event);

        assertEquals(OrderStatus.CANCELED, order.getStatus());

        verify(processedEventRepository).save(captorProcessedEvent.capture());

        assertEquals(eventId, captorProcessedEvent.getValue().getEventId());
    }

    @Test
    void shouldIgnoreAlreadyProcessedPaymentSucceededEvent() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var event = PaymentSucceededEvent.create(eventId, orderId, paymentId, Instant.now());

        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

        orderPaymentResultService.handlePaymentSucceededEvent(event);

        verifyNoInteractions(orderRepository);
        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }

    @Test
    void shouldIgnoreAlreadyProcessedPaymentFailedEvent() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var event = PaymentFailedEvent.create(eventId, orderId, paymentId, "Insufficient funds", Instant.now());

        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

        orderPaymentResultService.handlePaymentFailedEvent(event);

        verifyNoInteractions(orderRepository);
        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var event = PaymentSucceededEvent.create(eventId, orderId, paymentId, Instant.now());

        when(processedEventRepository.existsById(event.eventId())).thenReturn(false);
        when(orderRepository.findById(event.orderId())).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderPaymentResultService.handlePaymentSucceededEvent(event));

        verify(processedEventRepository, never()).save(any(ProcessedEvent.class));
    }
}