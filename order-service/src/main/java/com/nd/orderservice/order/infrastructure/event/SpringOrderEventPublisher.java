package com.nd.orderservice.order.infrastructure.event;

import com.nd.orderservice.order.application.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class SpringOrderEventPublisher implements OrderEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
