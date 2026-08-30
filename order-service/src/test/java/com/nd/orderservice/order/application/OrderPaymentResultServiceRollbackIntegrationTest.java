package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.event.PaymentSucceededEvent;
import com.nd.orderservice.order.domain.Order;
import com.nd.orderservice.order.domain.OrderStatus;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEvent;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
public class OrderPaymentResultServiceRollbackIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ProcessedEventRepository processedEventRepositoryMock;

    @Autowired
    private OrderPaymentResultService orderPaymentResultService;

    @Test
    void shouldRollbackOrderStatusWhenProcessedEventSavingFails() {
        var eventId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();
        var order = Order.create(UUID.randomUUID());
        var savedOrder = orderRepository.saveAndFlush(order);

        var event = new PaymentSucceededEvent(eventId, savedOrder.getId(), paymentId, Instant.now());

        when(processedEventRepositoryMock.existsById(eventId)).thenReturn(false);
        when(processedEventRepositoryMock.save(any(ProcessedEvent.class))).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> orderPaymentResultService.handlePaymentSucceededEvent(event));

        savedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();

        assertEquals(OrderStatus.CREATED, savedOrder.getStatus());
    }
}
