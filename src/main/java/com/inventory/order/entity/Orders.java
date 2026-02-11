package com.inventory.order.entity;



import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_customer", columnList = "customer_id"),
           @Index(name = "idx_orders_store", columnList = "store_id"),
           @Index(name = "idx_orders_status", columnList = "order_status")
       })
@ToString(exclude = {"items", "customer", "store"})
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;

    @NotNull
    @Column(name = "order_tms", nullable = false, columnDefinition = "TIMESTAMP")
    private Instant orderTms;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_customer"))
    private Customers customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_orders_store"))
    private Stores store;

    
    @Column(name = "order_status", length = 10, nullable = false)
    private String orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Order_Items> items = new LinkedHashSet<>();

    // convenience
//    public void addItem(Order_Items item) {
//        this.items.add(item);
//        item.setOrder(this);
//    }
}