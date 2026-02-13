package com.inventory.order.dto.response;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Integer id;
    private Instant orderTms;
    private String orderStatus;

    private Integer customerId;
    private Integer storeId;

    private List<OrderItemResponse> items;
}