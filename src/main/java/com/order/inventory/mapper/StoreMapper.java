package com.order.inventory.mapper;
 
import com.order.inventory.dto.StoreDTO;
import com.order.inventory.entity.Store;
import org.mapstruct.*;
 
@Mapper(componentModel = "spring")
public interface StoreMapper {
    StoreDTO toDto(Store e);
}