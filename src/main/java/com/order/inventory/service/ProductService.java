package com.order.inventory.service;

import com.order.inventory.dto.ProductDTO;
import com.order.inventory.entity.Product;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.ProductMapper;
import com.order.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    // Whitelist sort fields to avoid runtime errors on unknown properties
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "productName", "unitPrice", "brand", "colour", "size", "rating"
    );

    /**
     * GET /products  – supports optional ?sort=field
     * If sort is provided and invalid, we throw 400 to match CSV guidance on invalid sort field.
     */
    public List<ProductDTO> getAll(String sortField) {
        Sort sort = Sort.unsorted();
        if (sortField != null && !sortField.isBlank()) {
            if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
                throw new BadRequestException("Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
            }
            sort = Sort.by(sortField).ascending();
        }
        return repo.findAll(sort).stream().map(mapper::toDto).toList();
    }

    /**
     * GET /products/sort?field=value – explicit CSV endpoint, requires a valid field.
     */
    public List<ProductDTO> getAllStrict(String field) {
        if (field == null || field.isBlank() || !ALLOWED_SORT_FIELDS.contains(field)) {
            throw new BadRequestException("Invalid sort field. Allowed: " + ALLOWED_SORT_FIELDS);
        }
        return repo.findAll(Sort.by(field).ascending()).stream().map(mapper::toDto).toList();
    }

    /**
     * POST /products – create
     */
    public ProductDTO create(ProductDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank() || dto.getUnitPrice() == null) {
            throw new BadRequestException("Name and unitPrice are required");
        }
        if (dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("unitPrice cannot be negative");
        }
        Product e = mapper.toEntity(dto);
        return mapper.toDto(repo.save(e));
    }

    /**
     * PUT /products/{id} – update
     */
    public ProductDTO update(Integer id, ProductDTO dto) {
        Product p = repo.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        if (dto.getName() != null) p.setProductName(dto.getName());
        if (dto.getBrand() != null) p.setBrand(dto.getBrand());
        if (dto.getColour() != null) p.setColour(dto.getColour());
        if (dto.getSize() != null) p.setSize(dto.getSize());
        if (dto.getRating() != null) p.setRating(dto.getRating());
        if (dto.getUnitPrice() != null) {
            if (dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("unitPrice cannot be negative");
            }
            p.setUnitPrice(dto.getUnitPrice());
        }
        return mapper.toDto(repo.save(p));
    }

    /**
     * DELETE /products/{id}
     */
    public void delete(Integer id) {
        if (!repo.existsById(id)) throw new NotFoundException("Product not found");
        repo.deleteById(id);
    }

    /**
     * GET /products/brand/{brand}
     * 404 if none
     */
    public List<ProductDTO> byBrand(String brand) {
        List<ProductDTO> out = repo.findByBrandIgnoreCase(brand).stream().map(mapper::toDto).toList();
        if (out.isEmpty()) throw new NotFoundException("No products found for brand: " + brand);
        return out;
    }

    /**
     * GET /products/colour/{colour}
     * 404 if none
     */
    public List<ProductDTO> byColour(String colour) {
        List<ProductDTO> out = repo.findByColourIgnoreCase(colour).stream().map(mapper::toDto).toList();
        if (out.isEmpty()) throw new NotFoundException("No products found for colour: " + colour);
        return out;
    }

    /**
     * GET /products/unitprice?min&max
     */
    public List<ProductDTO> byPrice(BigDecimal min, BigDecimal max) {
        if (min == null || max == null || min.compareTo(max) > 0) {
            throw new BadRequestException("Invalid min/max price");
        }
        if (min.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("min cannot be negative");
        }
        return repo.findByUnitPriceBetween(min, max).stream().map(mapper::toDto).toList();
    }

    /**
     * GET /products/{productname}
     * 404 if none
     */
    public List<ProductDTO> byName(String name) {
        List<ProductDTO> out = repo.searchByName(name).stream().map(mapper::toDto).toList();
        if (out.isEmpty()) throw new NotFoundException("No products found matching name: " + name);
        return out;
    }
}