package com.order.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @Column(name = "product_id")
    private Integer id;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 45)
    private String colour;

    @Column(length = 45)
    private String brand;

    @Column(length = 10)
    private String size;

    private Integer rating;
}