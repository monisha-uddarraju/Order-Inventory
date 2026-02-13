package com.inventory.order.controller;

import com.inventory.order.dto.request.*;
import com.inventory.order.dto.response.ProductResponse;
import com.inventory.order.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public List<ProductResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody UpdateProductRequest req) {
        return ResponseEntity.ok(service.update(req));
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> delete(@PathVariable Integer id) {
//        service.delete(id);
//        return ResponseEntity.ok("Product deleted");
//    }

    @GetMapping("/name/{name}")
    public List<ProductResponse> search(@PathVariable String name) {
        return service.searchByName(name);
    }

    @GetMapping("/unitprice")
    public List<ProductResponse> byPrice(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return service.filterByUnitPrice(min, max);
    }

    @GetMapping("/sort")
    public List<ProductResponse> sort(@RequestParam String field) {
        return service.sort(field);
    }
}
//5