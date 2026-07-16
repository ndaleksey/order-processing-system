package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.command.CreateOrderCommand;
import com.nd.orderservice.order.domain.Order;
import com.nd.orderservice.order.domain.OrderItem;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventFactory;
import com.nd.orderservice.order.persistence.OrderRepository;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventFactory outboxEventFactory;

    @Transactional
    public Order create(CreateOrderCommand command) {
        var order = Order.create(command.customerId());

        for (var item : command.items()) {
            order.addItem(OrderItem.create(item.productId(), item.name(), item.productPrice(), item.quantity()));
        }

        var savedOrder = repository.save(order);

        var outboxEvent = outboxEventFactory.createOrderCreated(savedOrder);

        outboxEventRepository.save(outboxEvent);

        return savedOrder;
    }
}
