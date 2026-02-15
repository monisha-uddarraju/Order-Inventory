package com.order.inventory.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {
    private Integer id;
    private String name;
    private BigDecimal unitPrice;
    private String colour;
    private String brand;
    private String size;
    private Integer rating;
}