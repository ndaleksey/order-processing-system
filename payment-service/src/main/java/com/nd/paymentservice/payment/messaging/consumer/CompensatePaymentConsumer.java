package com.nd.paymentservice.payment.messaging.consumer;

import com.nd.paymentservice.payment.application.PaymentCompensationService;
import com.nd.paymentservice.payment.application.command.CompensatePaymentCommandMapper;
import com.nd.paymentservice.payment.messaging.event.PaymentCompensationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * @since 2026
 */
@Component
@RequiredArgsConstructor
public class CompensatePaymentConsumer {

    private final CompensatePaymentCommandMapper mapper;
    private final PaymentCompensationService compensationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topics.payment-requests}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void compensate(String payload) {
        var event = objectMapper.convertValue(payload, PaymentCompensationRequestedEvent.class);
        var command = mapper.toCommand(event);

        compensationService.compensate(command);
    }
}
