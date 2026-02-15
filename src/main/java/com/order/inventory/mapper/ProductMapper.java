package com.order.inventory.mapper;

import com.order.inventory.dto.ProductDTO;
import com.order.inventory.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "productName", target = "name")
    ProductDTO toDto(Product e);

    @InheritInverseConfiguration
    Product toEntity(ProductDTO dto);
}