package com.nd.paymentservice.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * @since 2026
 */
@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_payments_order_id",
                columnNames = "order_id"
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private Payment(UUID orderId, BigDecimal amount) {
        this.orderId = Objects.requireNonNull(orderId);
        this.amount = Objects.requireNonNull(amount);
        this.status = PaymentStatus.CREATED;

        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Payment create(UUID orderId, BigDecimal amount) {
        Objects.requireNonNull(amount, "Payment amount must not be null");

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        return new Payment(orderId, amount);
    }

    public void succeed() {
        this.status = PaymentStatus.SUCCEEDED;
        this.updatedAt = Instant.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
        this.updatedAt = Instant.now();
    }
}
