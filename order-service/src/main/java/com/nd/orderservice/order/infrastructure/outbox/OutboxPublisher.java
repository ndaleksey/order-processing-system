package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.infrastructure.kafka.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer orderEventProducer;

    @SuppressWarnings("unused")
    public void publishPendingEvents() {
        outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()
                .forEach(this::sendEvent);
    }

    private void sendEvent(OutboxEvent event) {
        orderEventProducer.send(event.getAggregateId(), event.getPayload())
                .thenAccept(result -> {
                    var metadata = result.getRecordMetadata();
                    log.info("Sent to topic = {}, partition = {}, offset = {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                    event.markPublished();
                })
                .exceptionally(e -> {
                    log.error("Kafka send failed", e);
                    return null;
                });
    }
}
