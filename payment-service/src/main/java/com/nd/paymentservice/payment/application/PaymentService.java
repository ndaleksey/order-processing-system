package com.nd.paymentservice.payment.application;

import com.nd.paymentservice.payment.domain.Payment;
import com.nd.paymentservice.payment.messaging.event.OrderCreatedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.persistence.PaymentRepository;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            log.info("OrderCreated event already processed: eventId={}", event.eventId());
            return;
        }

        var payment = Payment.create(event.orderId(), event.totalAmount());

        paymentRepository.save(payment);
        processedEventRepository.save(ProcessedEvent.create(event.eventId()));
    }
}
