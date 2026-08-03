package com.nd.paymentservice.payment.persistence;

import com.nd.paymentservice.payment.domain.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * @since 2026
 */
@ActiveProfiles("test")
@SpringBootTest
class PaymentRepositoryIntegrationTest {
    @Autowired
    private PaymentRepository paymentRepository;


    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void shouldRejectDuplicatePaymentForSameOrderId() {
        var orderId = UUID.randomUUID();
        var firstPayment = Payment.create(orderId, BigDecimal.valueOf(100L));
        var secondPayment = Payment.create(orderId, BigDecimal.valueOf(200L));

        paymentRepository.saveAndFlush(firstPayment);
        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository.saveAndFlush(secondPayment));
    }
}