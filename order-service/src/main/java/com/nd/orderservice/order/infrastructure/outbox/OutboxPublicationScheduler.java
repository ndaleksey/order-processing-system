package com.nd.orderservice.order.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @since 2026
 */
@Profile("!test")
@Component
@RequiredArgsConstructor
public class OutboxPublicationScheduler {
    private final OutboxPublisher outboxPublisher;

    @Scheduled(cron = "${app.kafka.publication.cron}")
    public void publish() {
        outboxPublisher.publishPendingEvents();
    }
}
