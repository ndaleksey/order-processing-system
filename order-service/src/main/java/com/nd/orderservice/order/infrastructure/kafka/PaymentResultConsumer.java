package com.nd.orderservice.order.infrastructure.kafka;

import com.nd.orderservice.order.application.OrderPaymentResultService;
import com.nd.orderservice.order.application.event.PaymentEvent;
import com.nd.orderservice.order.application.event.PaymentFailedEvent;
import com.nd.orderservice.order.application.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final ObjectMapper objectMapper;
    private final OrderPaymentResultService orderPaymentResultService;

    @KafkaListener(
            topics = "${app.kafka.topics.payments}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        var event = objectMapper.readValue(payload, PaymentEvent.class);

        switch (event) {
            case PaymentSucceededEvent e -> orderPaymentResultService.handlePaymentSucceededEvent(e);
            case PaymentFailedEvent e -> orderPaymentResultService.handlePaymentFailedEvent(e);
        }
    }
}
