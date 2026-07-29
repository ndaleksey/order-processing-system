package com.nd.orderservice.order.persistence;

import com.nd.orderservice.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**

 * @since 2026
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
}
