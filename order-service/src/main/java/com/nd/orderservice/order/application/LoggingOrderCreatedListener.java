package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Component
@Slf4j
public class LoggingOrderCreatedListener {

    @EventListener
    public void on(OrderCreatedEvent event) {
        log.info("Order created: {}", event);
    }
}
