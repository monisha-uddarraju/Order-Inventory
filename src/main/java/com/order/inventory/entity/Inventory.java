package com.order.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(name = "inventory_store_product_u", columnNames = {"store_id", "product_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "inventory_store_id_fk"))
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "inventory_product_id_fk"))
    private Product product;

    @Column(name = "product_inventory", nullable = false)
    private Integer productInventory;
}
