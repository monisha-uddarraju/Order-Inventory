package com.inventory.order.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products",
       indexes = @Index(name = "idx_products_name", columnList = "product_name"))
@ToString(exclude = {"inventories", "orderItems"})
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer id;

    @NotBlank
    @Column(name = "product_name", length = 255, nullable = false)
    private String productName;

    @PositiveOrZero
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "colour", length = 45)
    private String colour;

    @Column(name = "brand", length = 45)
    private String brand;

    @Column(name = "size", length = 10)
    private String size;

    @Column(name = "rating")
    private Integer rating;

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private Set<Inventory> inventories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private Set<Order_Items> orderItems = new LinkedHashSet<>();
}