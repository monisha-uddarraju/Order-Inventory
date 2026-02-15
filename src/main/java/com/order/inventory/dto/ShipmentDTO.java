package com.order.inventory.dto;
 
import lombok.*;
 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShipmentDTO {
    private Integer id;
    private Integer storeId;
    private Integer customerId;
    private String deliveryAddress;
    private String status;
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatusCount {
        private String status;
        private Long count;      // e.g., distinct customers per status
    }
 
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SoldCount {
        private String status;
        private Long totalSold;  // sum of quantities
    }
}