package com.nd.orderservice.order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @since 2026
 */

@Getter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;

    private UUID customerId;

    public static Order create(UUID customerId) {
        var order = new Order();
        order.customerId = customerId;
        order.status = OrderStatus.CREATED;
        order.totalPrice = BigDecimal.ZERO;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        return order;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalcTotal();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalcTotal();
    }

    public void markConfirmed() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void changeQuantity(UUID itemId, Integer quantity) {
        var item = findItem(itemId);

        item.changeQuantity(quantity);

        recalcTotal();
    }

    private OrderItem findItem(UUID itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void recalcTotal() {
        this.totalPrice = items.stream()
                .map(i -> i.getProductPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
