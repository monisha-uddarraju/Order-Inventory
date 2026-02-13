package com.inventory.order.mapper;



import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.inventory.order.dto.request.CreateProductRequest;
import com.inventory.order.dto.response.ProductResponse;
import com.inventory.order.entity.Products;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    // Entity -> DTO
    ProductResponse toResponse(Products entity);

    List<ProductResponse> toResponseList(List<Products> entities);

    // Create DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Products fromCreateRequest(CreateProductRequest dto);
}//1