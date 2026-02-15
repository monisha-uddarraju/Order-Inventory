package com.order.inventory.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerDTO {
    private Integer id;
    private String email;
    private String fullName;
}