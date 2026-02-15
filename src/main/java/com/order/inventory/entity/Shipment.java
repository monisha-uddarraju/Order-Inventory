package com.order.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shipments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment {

    @Id
    @Column(name = "shipment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
                foreignKey = @ForeignKey(name = "shipments_store_id_fk"))
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
                foreignKey = @ForeignKey(name = "shipments_customer_id_fk"))
    private Customer customer;

    @Column(name = "delivery_address", length = 512)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", length = 100)
    private ShipmentStatus shipmentStatus;
}
