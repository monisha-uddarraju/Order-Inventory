package com.order.inventory.dto;

import lombok.*;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDTO {
    private Integer id;
    private Instant orderTms;
    private String status;
    private Integer customerId;
    private Integer storeId;

    // NEW: populated from Order.store
    private String storeName;
    private String webAddress;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LineItem {
        private Integer lineItemId;
        private Integer productId;
        private String productName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private String shipmentStatus;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatusCount {
        private String status;
        private Long count;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Details {
        private Integer orderId;
        private String storeName;
        private String shipmentStatus;
        private List<LineItem> items;
        private BigDecimal totalAmount;
    }
}