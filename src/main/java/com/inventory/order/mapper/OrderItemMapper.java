package com.inventory.order.mapper;

import org.mapstruct.Mapper;
import com.inventory.order.dto.response.OrderItemResponse;
import com.inventory.order.entity.Order_Items;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponse toResponse(Order_Items entity);
}//1