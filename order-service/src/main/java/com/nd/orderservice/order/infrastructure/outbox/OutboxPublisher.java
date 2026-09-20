package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.infrastructure.kafka.KafkaEventProducer;
import com.nd.orderservice.order.infrastructure.kafka.KafkaTopicNameResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 2026
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final KafkaTopicNameResolver kafkaTopicNameResolver;

    @SuppressWarnings("unused")
    @Transactional
    public void publishPendingEvents() {
        outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()
                .forEach(event -> {
                    var sendingResult = kafkaEventProducer.send(
                                    kafkaTopicNameResolver.resolve(event.getType()),
                                    event.getAggregateId(),
                                    event.getPayload())
                            .join();

                    var metadata = sendingResult.getRecordMetadata();

                    log.info("Published outbox event [id = {}, topic = {}, partition = {}, offset = {}]",
                            event.getId(), metadata.topic(), metadata.partition(), metadata.offset());

                    event.markPublished();
                });
    }
}
