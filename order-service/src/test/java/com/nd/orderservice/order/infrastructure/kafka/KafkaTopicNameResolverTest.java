package com.nd.orderservice.order.infrastructure.kafka;

import com.nd.orderservice.order.infrastructure.outbox.OutboxEventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 2026
 */
class KafkaTopicNameResolverTest {
    @Test
    void resolveName() {
        var topics = new KafkaTopicsProperties(
                "orders",
                "payments",
                "inventory"
        );

        var resolver = new KafkaTopicNameResolver(topics);

        assertEquals(topics.orders(), resolver.resolve(OutboxEventType.ORDER_CREATED));
        assertEquals(topics.inventory(), resolver.resolve(OutboxEventType.INVENTORY_RESERVATION_REQUESTED));
    }

}