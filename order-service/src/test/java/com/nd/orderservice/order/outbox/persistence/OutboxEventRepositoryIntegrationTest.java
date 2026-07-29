package com.nd.orderservice.order.outbox.persistence;

import com.nd.orderservice.order.domain.Order;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventFactory;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @since 2026
 */
@ActiveProfiles("test")
@SpringBootTest
public class OutboxEventRepositoryIntegrationTest {
    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private OutboxEventFactory outboxEventFactory;

    @Transactional
    @Test
    void shouldReturnOnlyUnpublishedEvents() {
        var customerId = UUID.randomUUID();
        var order = Order.create(customerId);

        var event1 = outboxEventFactory.createOrderCreated(order);


        var event2 = outboxEventFactory.createOrderCreated(order);
        event2.markPublished();

        outboxEventRepository.save(event1);
        outboxEventRepository.save(event2);

        entityManager.flush();
        entityManager.clear();

        var events = outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();

        assertEquals(1, events.size());
        assertEquals(event1.getId(), events.getFirst().getId());
        assertFalse(events.stream()
                .anyMatch(event -> event.getId().equals(event2.getId()))
        );
    }
}
