package com.nd.inventoryservice.inventory.messaging.outbox;

import com.nd.inventoryservice.inventory.messaging.kafka.KafkaEventProducer;
import com.nd.inventoryservice.inventory.messaging.kafka.KafkaTopicsProperties;
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
public class OutboxEventPublisher {

    private final OutboxProperties outboxProperties;
    private final KafkaEventProducer eventProducer;
    private final OutboxEventRepository eventRepository;
    private final KafkaTopicsProperties topicsProperties;

    @Transactional
    public void publishPendingEvents() {
        eventRepository.findByPublishedAtIsNullOrderByCreatedAtAsc(
                        Limit.of(outboxProperties.batchSize()))
                .forEach(e -> {
                    var result = eventProducer.send(topicsProperties.inventoryResults(), e.getAggregateId(), e.getPayload())
                            .join();

                    var metadata = result.getRecordMetadata();

                    log.info("Published outbox e [id = {}, topic = {}, partition = {}, offset = {}]",
                            e.getId(), metadata.topic(), metadata.partition(), metadata.offset());

                    e.markPublished();
                });
    }

}
