package com.nd.orderservice.order.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**

 * @since 2026
 */

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID productId;
    private String name;
    private BigDecimal productPrice;
    private Integer quantity;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    public static OrderItem create(UUID productId, String name, BigDecimal productPrice, int quantity) {
        var orderItem = new OrderItem();
        orderItem.productId = productId;
        orderItem.name = name;
        orderItem.productPrice = productPrice;
        orderItem.quantity = quantity;
        return orderItem;
    }

    @Override
    public String toString() {
        return productId.toString() + " " + name + " " + productPrice.toString();
    }

    void changeQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}