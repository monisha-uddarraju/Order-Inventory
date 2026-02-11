package com.inventory.order.mapper;

import org.mapstruct.Mapper;
import com.inventory.order.dto.response.ProductResponse;
import com.inventory.order.dto.request.CreateProductRequest;
import com.inventory.order.entity.Products;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Products entity);
    Products fromCreateRequest(CreateProductRequest dto);
}