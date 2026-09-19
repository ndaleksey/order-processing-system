package com.nd.inventoryservice.inventory.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(updatable = false, nullable = false)
    private OutboxEventType type;

    @Column(updatable = false, nullable = false)
    private UUID aggregateId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;


    public static OutboxEvent inventoryReserved(
            UUID eventId,
            UUID orderId,
            Instant occurredAt,
            String payload
    ) {
        return create(eventId, orderId, occurredAt, payload);
    }

    private static OutboxEvent create(UUID eventId,
                                      UUID orderId,
                                      Instant occurredAt,
                                      String payload) {
        var event = new OutboxEvent();
        event.id = eventId;
        event.type = OutboxEventType.INVENTORY_RESERVED;
        event.aggregateId = orderId;
        event.createdAt = occurredAt;
        event.payload = payload;

        return event;
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }
}
