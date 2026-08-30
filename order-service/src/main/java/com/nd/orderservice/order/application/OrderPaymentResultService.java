package com.nd.orderservice.order.application;

import com.nd.orderservice.order.application.event.PaymentFailedEvent;
import com.nd.orderservice.order.application.event.PaymentSucceededEvent;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEvent;
import com.nd.orderservice.order.infrastructure.idempotency.ProcessedEventRepository;
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
public class OrderPaymentResultService {
    private final ProcessedEventRepository processedEventRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void handlePaymentSucceededEvent(PaymentSucceededEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("PaymentSucceededEvent event already processed: eventId={}", event.eventId());

            return;
        }

        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.orderId()));

        order.markConfirmed();

        processedEventRepository.save(ProcessedEvent.create(event.eventId()));
    }

    @Transactional
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("PaymentFailedEvent event already processed: eventId={}", event.eventId());

            return;
        }

        var order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + event.orderId()));

        order.markCanceled();

        processedEventRepository.save(ProcessedEvent.create(event.eventId()));
    }
}
