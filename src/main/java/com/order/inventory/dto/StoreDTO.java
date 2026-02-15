package com.order.inventory.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreDTO {
    private Integer id;
    private String storeName;
    private String webAddress;
    private String physicalAddress;
}