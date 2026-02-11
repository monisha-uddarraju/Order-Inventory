package com.inventory.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stores",
       indexes = @Index(name = "idx_stores_name", columnList = "store_name"))
@ToString(exclude = {"inventories", "orders", "shipments", "logo"})
public class Stores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer id;

    @Column(name = "store_name", length = 255, nullable = false)
    private String storeName;

    @Column(name = "web_address", length = 100)
    private String webAddress;

    @Column(name = "physical_address", length = 512)
    private String physicalAddress;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Lob
    @Column(name = "logo")
    private byte[] logo;

    @Column(name = "logo_mime_type", length = 512)
    private String logoMimeType;

    @Column(name = "logo_filename", length = 512)
    private String logoFilename;

    @Column(name = "logo_charset", length = 512)
    private String logoCharset;

    @Column(name = "logo_last_updated")
    private LocalDate logoLastUpdated;

    @OneToMany(mappedBy = "store")
    @Builder.Default
    private Set<Inventory> inventories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "store")
    @Builder.Default
    private Set<Orders> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "store")
    @Builder.Default
    private Set<Shipments> shipments = new LinkedHashSet<>();
}