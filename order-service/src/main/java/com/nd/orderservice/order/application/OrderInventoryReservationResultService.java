package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.command.InventoryReservationFailedCommand;
import com.nd.orderservice.order.application.command.InventoryReservationSucceededCommand;
import com.nd.orderservice.order.application.exception.OrderNotFoundException;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEvent;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEventRepository;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventFactory;
import com.nd.orderservice.order.infrastructure.outbox.OutboxEventRepository;
import com.nd.orderservice.order.persistence.OrderRepository;
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
public class OrderInventoryReservationResultService {

    private final ProcessedEventRepository processedEventRepository;
    private final OrderRepository orderRepository;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void handle(InventoryReservationSucceededCommand command) {
        if (processedEventRepository.existsById(command.eventId())) {
            log.info("InventoryReservationSucceeded event already proceeded: eventId = {}", command.eventId());

            return;
        }

        var order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + command.orderId()));

        order.markConfirmed();

        processedEventRepository.save(ProcessedEvent.create(command.eventId()));
    }

    @Transactional
    public void handle(InventoryReservationFailedCommand command) {
        if (processedEventRepository.existsById(command.eventId())) {
            log.info("InventoryReservationFailed event already proceeded: eventId = {}", command.eventId());

            return;
        }

        var order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(("Order not found: " + command.orderId())));

        var event = outboxEventFactory.createPaymentCompensationRequested(order);
        outboxEventRepository.save(event);

        processedEventRepository.save(ProcessedEvent.create(command.eventId()));
    }
}
