package com.inventory.order.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shipments",
       indexes = {
           @Index(name = "idx_shipments_store", columnList = "store_id"),
           @Index(name = "idx_shipments_customer", columnList = "customer_id"),
           @Index(name = "idx_shipments_status", columnList = "shipment_status")
       })
@ToString(exclude = {"orderItems", "store", "customer"})
public class Shipments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipment_id")
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_shipments_store"))
    private Stores store;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_shipments_customer"))
    private Customers customer;

    @NotBlank
    @Column(name = "delivery_address", length = 512, nullable = false)
    private String deliveryAddress;

    
    @Column(name = "shipment_status", length = 100, nullable = false)
    private String shipmentStatus;

    @OneToMany(mappedBy = "shipment")
    @Builder.Default
    private Set<Order_Items> orderItems = new LinkedHashSet<>();
}
