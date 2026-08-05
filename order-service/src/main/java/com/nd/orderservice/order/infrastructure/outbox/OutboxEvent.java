package com.nd.orderservice.order.infrastructure.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * @since 2026
 */
@Getter
@NoArgsConstructor
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    private UUID id;

    private String type;

    @Column(name = "aggregated_id", nullable = false)
    private UUID aggregateId;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Instant publishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    public static OutboxEvent orderCreated(
            UUID eventId,
            UUID orderId,
            Instant createdAt,
            String payload) {
        var event = new OutboxEvent();
        event.id = eventId;
        event.type = "ORDER_CREATED";
        event.aggregateId = orderId;
        event.createdAt = createdAt;
        event.payload = payload;

        return event;
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }
}
