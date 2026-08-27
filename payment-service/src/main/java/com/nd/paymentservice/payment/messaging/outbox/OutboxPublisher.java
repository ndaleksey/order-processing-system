package com.nd.paymentservice.payment.messaging.outbox;

import com.nd.paymentservice.payment.messaging.kafka.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxProperties outboxProperties;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public void publishPendingEvents() {
        outboxEventRepository.findByPublishedAtIsNullOrderByCreatedAtAsc(
                        Limit.of(outboxProperties.batchSize()))
                .forEach(e -> {
                    var result = paymentEventProducer.send(e.getAggregatedId(), e.getPayload()).join();

                    var metadata = result.getRecordMetadata();

                    log.info("Published outbox event [id = {}, topic = {}, partition = {}, offset = {}]",
                            e.getId(), metadata.topic(), metadata.partition(), metadata.offset());

                    e.markPublished();
                });
    }
}
