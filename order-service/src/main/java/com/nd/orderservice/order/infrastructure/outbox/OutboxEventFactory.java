package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.application.event.OrderCreatedEvent;
import com.nd.orderservice.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEvent createOrderCreated(Order order) {
        var eventId = UUID.randomUUID();
        var occurredAt = Instant.now();

        var event = new OrderCreatedEvent(
                eventId,
                order.getId(),
                order.getCustomerId(),
                order.getTotalPrice(),
                occurredAt);

        var payload = objectMapper.writeValueAsString(event);

        return OutboxEvent.orderCreated(
                eventId,
                order.getId(),
                occurredAt,
                payload
        );

    }
}
