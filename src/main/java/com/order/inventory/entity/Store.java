package com.order.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Store {

    @Id
    @Column(name = "store_id")
    private Integer id;

    @Column(name = "store_name", length = 255)
    private String storeName;

    @Column(name = "web_address", length = 100)
    private String webAddress;

    @Column(name = "physical_address", length = 512)
    private String physicalAddress;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Lob
    private byte[] logo;

    @Column(name = "logo_mime_type", length = 512)
    private String logoMimeType;

    @Column(name = "logo_filename", length = 512)
    private String logoFilename;

    @Column(name = "logo_charset", length = 512)
    private String logoCharset;

    @Column(name = "logo_last_updated")
    private java.sql.Date logoLastUpdated;
}