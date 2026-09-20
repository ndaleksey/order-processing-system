package com.nd.orderservice.order.infrastructure.kafka;

import com.nd.orderservice.order.infrastructure.outbox.OutboxEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class KafkaTopicNameResolver {

    private final KafkaTopicsProperties topics;

    public String resolve(OutboxEventType eventType) {
        return switch (eventType) {
            case ORDER_CREATED -> topics.orders();
            case INVENTORY_RESERVATION_REQUESTED -> topics.inventory();
            case PAYMENT_COMPENSATION_REQUESTED -> topics.paymentRequests();
        };
    }
}
