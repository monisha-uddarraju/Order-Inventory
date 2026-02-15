package com.order.inventory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "order_items",
    uniqueConstraints = @UniqueConstraint(
        name = "order_items_product_u",
        columnNames = {"product_id", "order_id"}
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {

    // NOTE: Matches current DB where order_id is the PRIMARY KEY in order_items.
    // We do NOT use @GeneratedValue here to avoid conflicts with the FK to orders.
    @Id
    @Column(name = "order_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "order_id",
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(name = "order_items_order_id_fk")
    )
    private Order order;

    @Column(name = "line_item_id", nullable = false)
    private Integer lineItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "order_items_product_id_fk")
    )
    private Product product;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "shipment_id",
        foreignKey = @ForeignKey(name = "order_items_shipment_id_fk")
    )
    private Shipment shipment;
}