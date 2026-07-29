package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.command.CreateOrderCommand;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEvent;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 2026
 */
@SpringBootTest
class OrderServiceRollbackIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private OutboxEventRepository outboxEventRepository;

    @Test
    void shouldRollbackOrderWhenOutboxPersistenceFails() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var items = List.of(new CreateOrderCommand.Item(productId, "Item 1", BigDecimal.valueOf(100), 10));

        var command = new CreateOrderCommand(customerId, items);

        var ordersBefore = orderRepository.count();

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenThrow(new RuntimeException("Outbox persistence failed"));

        assertThrows(RuntimeException.class, () -> orderService.create(command));

        assertEquals(ordersBefore, orderRepository.count());

        verify(outboxEventRepository).save(any(OutboxEvent.class));
    }
}
