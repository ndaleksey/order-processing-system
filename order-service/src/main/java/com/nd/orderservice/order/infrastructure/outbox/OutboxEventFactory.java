package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.application.event.OrderCreatedEvent;
import com.nd.orderservice.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    private final ObjectMapper objectMapper;

    public OutboxEvent createOrderCreated(Order order) {
        var event = new OrderCreatedEvent(order.getId(), order.getCustomerId());

        return OutboxEvent.orderCreated(order.getId(), objectMapper.writeValueAsString(event)
        );

    }
}
