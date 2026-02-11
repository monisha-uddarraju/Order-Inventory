package com.inventory.order.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(name = "uk_inventory_store_product",
                                             columnNames = {"store_id", "product_id"}),
       indexes = {
           @Index(name = "idx_inventory_store", columnList = "store_id"),
           @Index(name = "idx_inventory_product", columnList = "product_id")
       })
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_inventory_store"))
    private Stores store;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_inventory_product"))
    private Products product;

    @Min(0)
    @Column(name = "product_inventory", nullable = false)
    private Integer productInventory;
}
