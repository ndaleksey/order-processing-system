package com.nd.paymentservice.payment.messaging.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox")
public class OutboxEvent {
    @Id
    @UuidGenerator
    private UUID id;

    private String type;

    @Column(name = "aggregated_id", nullable = false)
    private UUID aggregatedId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at", nullable = false, updatable = false)
    private Instant publishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    public static OutboxEvent createSucceeded(
            UUID eventId,
            UUID orderId,
            Instant createdAt,
            String payload
    ) {
        return create(eventId, orderId, createdAt, "PAYMENT_SUCCEEDED", payload);
    }

    public static OutboxEvent createFailed(
            UUID eventId,
            UUID orderId,
            Instant createdAt,
            String payload) {
        return create(eventId, orderId, createdAt, "PAYMENT_FAILED", payload);
    }

    public void markPublished() {
        this.publishedAt = Instant.now();
    }

    private static OutboxEvent create(UUID eventId,
                                      UUID orderId,
                                      Instant createdAt,
                                      String type,
                                      String payload) {
        var event = new OutboxEvent();
        event.id = eventId;
        event.aggregatedId = orderId;
        event.createdAt = createdAt;
        event.payload = payload;
        event.type = "PAYMENT_SUCCEEDED";

        return event;
    }
}
