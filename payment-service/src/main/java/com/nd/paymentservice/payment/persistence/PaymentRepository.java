package com.nd.paymentservice.payment.persistence;

import com.nd.paymentservice.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * @since 2026
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @SuppressWarnings("unused")
    Optional<Payment> findByOrderId(UUID orderId);

    @SuppressWarnings("unused")
    boolean existsByOrderId(UUID orderId);
}
