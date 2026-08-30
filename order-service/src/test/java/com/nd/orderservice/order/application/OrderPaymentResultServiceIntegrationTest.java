package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.event.PaymentSucceededEvent;
import com.nd.orderservice.order.domain.Order;
import com.nd.orderservice.order.domain.OrderStatus;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 2026
 */

@SpringBootTest
@ActiveProfiles("test")
class OrderPaymentResultServiceIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private OrderPaymentResultService orderPaymentResultService;


    @Test
    void shouldConfirmOrderAndSaveProcessedEventWhenPaymentSucceeded() {
        var eventId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var order = Order.create(UUID.randomUUID());
        var savedOrder = orderRepository.saveAndFlush(order);

        var event = new PaymentSucceededEvent(eventId, savedOrder.getId(), paymentId, Instant.now());

        orderPaymentResultService.handlePaymentSucceededEvent(event);

        savedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertEquals(OrderStatus.CONFIRMED, savedOrder.getStatus());

        var processedEvent = processedEventRepository.findById(eventId).orElseThrow();

        assertEquals(eventId, processedEvent.getEventId());
        assertNotNull(processedEvent.getProcessedAt());
    }
}