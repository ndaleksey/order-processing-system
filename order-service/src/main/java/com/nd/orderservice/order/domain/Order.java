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

    public static Order create() {
        var order = new Order();
        order.status = OrderStatus.CREATED;
        order.totalPrice = BigDecimal.ZERO;
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

    private void recalcTotal() {
        this.totalPrice = items.stream()
                .map(i -> i.getProductPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
