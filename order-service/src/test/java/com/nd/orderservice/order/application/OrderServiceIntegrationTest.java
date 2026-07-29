package com.nd.orderservice.order.application;


import com.nd.orderservice.order.application.command.CreateOrderCommand;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @since 2026
 */
@SpringBootTest
class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Test
    void create() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var items = List.of(new CreateOrderCommand.Item(productId, "Item 1", BigDecimal.valueOf(100), 10));

        var command = new CreateOrderCommand(customerId, items);

        var order = orderService.create(command);

        assertTrue(orderRepository.existsById(order.getId()));

        var persistedOrder = orderRepository.findById(order.getId())
                .orElseThrow();

        assertNotNull(persistedOrder.getItems());
        assertFalse(persistedOrder.getItems().isEmpty());

        assertEquals(1, persistedOrder.getItems().size());

        var persistedItem = persistedOrder.getItems().getFirst();

        assertEquals(productId, persistedItem.getProductId());
        assertEquals("Item 1", persistedItem.getProductName());
        assertEquals(10, persistedItem.getQuantity());

        var events = outboxEventRepository.findAll();

        assertNotNull(events);
        assertFalse(events.isEmpty());

        var eventOpt = events.stream()
                .filter(event -> event.getAggregateId().equals(order.getId()))
                .findAny();

        assertTrue(eventOpt.isPresent());

        var event = eventOpt.get();

        assertEquals(order.getId(), event.getAggregateId());
        assertEquals("ORDER_CREATED", event.getType());
    }
}