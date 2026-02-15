package com.order.inventory.controller;

import com.order.inventory.dto.ProductDTO;
import com.order.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    /**
     * CSV: GET /api/v1/products  – fetch all (optionally sorted with ?sort=field)
     * Returns 200 with list (can be empty).
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> all(@RequestParam(required = false) String sort) {
        return ResponseEntity.ok(service.getAll(sort));
    }

    /**
     * CSV: GET /api/v1/products/sort?field=value  – explicit sort endpoint
     * Throws 400 if field is missing/invalid.
     */
    @GetMapping("/sort")
    public ResponseEntity<List<ProductDTO>> sort(@RequestParam("field") String field) {
        return ResponseEntity.ok(service.getAllStrict(field));
    }

    /**
     * CSV: GET /api/v1/products/unitprice?min=value&max=value – filter by unit price range
     * Throws 400 for invalid min/max.
     */
    @GetMapping("/unitprice")
    public ResponseEntity<List<ProductDTO>> byPrice(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return ResponseEntity.ok(service.byPrice(min, max));
    }

    /**
     * CSV: GET /api/v1/products/brand/{brand} – filter by brand
     * Throws 404 if no matches.
     */
    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<ProductDTO>> byBrand(@PathVariable String brand) {
        return ResponseEntity.ok(service.byBrand(brand));
    }

    /**
     * CSV: GET /api/v1/products/colour/{colour} – filter by colour
     * Throws 404 if no matches.
     */
    @GetMapping("/colour/{colour}")
    public ResponseEntity<List<ProductDTO>> byColour(@PathVariable String colour) {
        return ResponseEntity.ok(service.byColour(colour));
    }

    /**
     * CSV: GET /api/v1/products/{productname} – wildcard search by name
     * Throws 404 if no matches.
     */
    @GetMapping("/{productname}")
    public ResponseEntity<List<ProductDTO>> byName(@PathVariable String productname) {
        return ResponseEntity.ok(service.byName(productname));
    }

    /**
     * CSV: POST /api/v1/products – create
     * Throws 400 if required fields missing.
     */
    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * CSV (we keep RESTful path): PUT /api/v1/products/{id} – update by id
     * Throws 404 if product not found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Integer id, @RequestBody ProductDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

//    /**
//     * CSV: DELETE /api/v1/products/{id} – delete by id
//     * Throws 404 if product not found.
//     */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }
}