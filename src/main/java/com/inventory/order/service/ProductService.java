package com.inventory.order.service;

import com.inventory.order.dto.request.CreateProductRequest;
import com.inventory.order.dto.request.UpdateProductRequest;
import com.inventory.order.dto.response.ProductResponse;
import com.inventory.order.entity.Products;
import com.inventory.order.exception.*;
import com.inventory.order.mapper.ProductMapper;
import com.inventory.order.repository.ProductsRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductsRepository repo;
    private final ProductMapper mapper;

    public ProductService(ProductsRepository repo, ProductMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<ProductResponse> getAll() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    public ProductResponse create(CreateProductRequest req) {
        if (repo.existsByProductName(req.productName()))
            throw new ResourceAlreadyExistsException("Product already exists");

        Products entity = mapper.fromCreateRequest(req);
        return mapper.toResponse(repo.save(entity));
    }

    public ProductResponse update(UpdateProductRequest req) {
        Products entity = repo.findById(req.id())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        entity.setProductName(req.productName());
        entity.setUnitPrice(req.unitPrice());
        entity.setColour(req.colour());
        entity.setBrand(req.brand());
        entity.setSize(req.size());
        entity.setRating(req.rating());

        return mapper.toResponse(entity);
    }

    public void delete(Integer id) {
        if (!repo.existsById(id))
            throw new ResourceNotFoundException("Product not found");
        repo.deleteById(id);
    }

    public List<ProductResponse> searchByName(String name) {
        return repo.findByProductNameContainingIgnoreCase(name)
                .stream().map(mapper::toResponse).toList();
    }

    public List<ProductResponse> sort(String field) {
        return repo.findAll(Sort.by(field)).stream().map(mapper::toResponse).toList();
    }

    public List<ProductResponse> filterByUnitPrice(BigDecimal min, BigDecimal max) {
        return repo.findByUnitPriceBetween(min, max).stream().map(mapper::toResponse).toList();
    }
}