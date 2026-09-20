package com.nd.paymentservice.payment.application;

import com.nd.paymentservice.payment.application.command.CompensatePaymentCommand;
import com.nd.paymentservice.payment.application.exception.PaymentNotFoundException;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEvent;
import com.nd.paymentservice.payment.messaging.idempotency.ProcessedEventRepository;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEventFactory;
import com.nd.paymentservice.payment.messaging.outbox.OutboxEventRepository;
import com.nd.paymentservice.payment.persistence.PaymentRepository;
import com.nd.paymentservice.payment.provider.PaymentProvider;
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
public class PaymentCompensationService {
    private final ProcessedEventRepository processedEventRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final OutboxEventFactory outboxEventFactory;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void compensate(CompensatePaymentCommand command) {
        if (processedEventRepository.existsById(command.eventId())) {
            log.info("CompensatePayment event already processed: eventId={}", command.eventId());

            return;
        }

        var payment = paymentRepository.findByOrderId(command.orderId())
                .orElseThrow(()-> new PaymentNotFoundException("Payment not found"));


        var result = paymentProvider.refund(command.orderId(), payment.getAmount());

        if (!result.successful()) {
            throw new RuntimeException("Payment refund failed");
        }

        payment.compensate();

        var event = outboxEventFactory.createPaymentCompensatedEvent(payment);
        outboxEventRepository.save(event);

        processedEventRepository.save(ProcessedEvent.create(event.getId()));
    }
}
