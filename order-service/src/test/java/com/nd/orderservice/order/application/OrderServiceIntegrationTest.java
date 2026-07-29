package com.nd.orderservice.order.application;


import com.nd.orderservice.order.application.command.CreateOrderCommand;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @since 2026
 */
@ActiveProfiles("test")
@SpringBootTest
class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private EntityManager entityManager;

    @Transactional
    @Test
    void shouldPersistOrderItemsAndOutboxEvent() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var items = List.of(new CreateOrderCommand.Item(productId, "Item 1", BigDecimal.valueOf(100), 10));

        var command = new CreateOrderCommand(customerId, items);

        var order = orderService.create(command);

        entityManager.flush();
        entityManager.clear();

        var persistedOrder = orderRepository.findById(order.getId())
                .orElseThrow();

        assertFalse(persistedOrder.getItems().isEmpty());

        assertEquals(1, persistedOrder.getItems().size());

        var persistedItem = persistedOrder.getItems().getFirst();

        assertEquals(productId, persistedItem.getProductId());
        assertEquals("Item 1", persistedItem.getProductName());
        assertEquals(10, persistedItem.getQuantity());

        var event = outboxEventRepository.findAll().stream()
                .filter(candidate -> candidate.getAggregateId().equals(order.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(order.getId(), event.getAggregateId());
        assertEquals("ORDER_CREATED", event.getType());
    }
}