package com.nd.orderservice.order.infrastructure.event;

import com.nd.orderservice.order.application.event.OrderCreatedEvent;

/**
 * @since 2026
 */
public interface OrderEventPublisher {

    void publish(OrderCreatedEvent event);
}
