package com.nd.orderservice.order.infrastructure.idempotency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @since 2026
 */
@SpringBootTest
@ActiveProfiles("test")
class ProcessedEventRepositoryTest {
    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void shouldSaveAndFindProcessedEventByEventId() {
        var eventId = UUID.randomUUID();
        var event = ProcessedEvent.create(eventId);

        processedEventRepository.saveAndFlush(event);

        var savedEvent = processedEventRepository.findById(eventId)
                .orElseThrow();

        assertEquals(eventId, savedEvent.getEventId());
        assertNotNull(savedEvent.getProcessedAt());
    }
}
