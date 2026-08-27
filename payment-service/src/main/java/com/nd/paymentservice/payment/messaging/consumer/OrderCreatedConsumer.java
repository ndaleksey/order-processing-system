package com.nd.paymentservice.payment.messaging.consumer;

import com.nd.paymentservice.payment.application.PaymentService;
import com.nd.paymentservice.payment.messaging.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${app.kafka.topics.orders}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        var event = objectMapper.readValue(payload, OrderCreatedEvent.class);

        paymentService.handleOrderCreated(event);
    }
}
