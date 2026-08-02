package com.nd.orderservice.order.infrastructure.outbox;

import com.nd.orderservice.order.infrastructure.kafka.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final OrderEventProducer orderEventProducer;

    @Scheduled(fixedRate = 10000)
    @SuppressWarnings("unused")
    @Transactional
    public void publishPendingEvents() {
        outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc()
                .forEach(event -> {
                    var sendingResult = orderEventProducer.send(event.getAggregateId(), event.getPayload())
                            .join();

                    var metadata = sendingResult.getRecordMetadata();

                    log.info("Published outbox event [id = {}, topic = {}, partition = {}, offset = {}]",
                            event.getId(), metadata.topic(), metadata.partition(), metadata.offset());

                    event.markPublished();
                });
    }
}
