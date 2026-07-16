package com.nd.orderservice.order.application.event;

/**
 * @since 2026
 */
public interface OrderEventPublisher {

    void publish(OrderCreatedEvent event);
}
