package com.inventory.order.mapper;

import org.mapstruct.Mapper;


import org.mapstruct.ReportingPolicy;
import com.inventory.order.dto.response.OrderResponse;
import com.inventory.order.entity.Orders;

@Mapper(
    componentModel = "spring",
    uses = { OrderItemMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {
    OrderResponse toResponse(Orders entity);
}//1