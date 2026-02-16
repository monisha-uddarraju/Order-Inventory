package com.order.inventory.controller;

import com.order.inventory.dto.ProductDTO;
import com.order.inventory.exception.BadRequestException;
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
//    @PostMapping
//    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO dto) {
//        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
//    }
    
//    @PostMapping
//    public ResponseEntity<String> create(@RequestBody ProductDTO dto) {
//
//        // Call service to create the product (but ignore returned DTO)
//        service.create(dto);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body("Record Created Successfully");
//    }
    
    @PostMapping
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Product creation payload (id must not be provided)",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            type = "object",
                            requiredProperties = { "name", "unitPrice" },
                            example = """
                            {
                              "name": "string",
                              "unitPrice": 0,
                              "colour": "string",
                              "brand": "string",
                              "size": "string",
                              "rating": 0
                            }
                            """
                    )
            )
    )
    public ResponseEntity<String> create(@RequestBody ProductDTO dto) {

        // ID MUST NOT BE PRESENT FOR CREATION
        if (dto.getId() != null) {
            throw new BadRequestException("Invalid request. Please provide valid product data for creation.");
        }

        // REQUIRED FIELD VALIDATION
        if (dto.getName() == null || dto.getName().isBlank()
                || dto.getUnitPrice() == null
                || dto.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {

            throw new BadRequestException("Invalid request. Please provide valid product data for creation.");
        }

        service.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Record Created Successfully");
    }

    /**
     * CSV (we keep RESTful path): PUT /api/v1/products/{id} – update by id
     * Throws 404 if product not found.
     */
//    @PutMapping
//    public ResponseEntity<ProductDTO> updateByObject(@RequestBody ProductDTO dto) {
//     
//        if (dto.getId() == null) {
//            throw new BadRequestException("Product id is required for update");
//        }
//     
//        return ResponseEntity.ok(service.update(dto.getId(), dto));
//    }
//     
    @PutMapping
    public ResponseEntity<String> updateByObject(@RequestBody ProductDTO dto) {

        if (dto.getId() == null) {
            throw new BadRequestException("Product id is required for update");
        }

        service.update(dto.getId(), dto);

        return ResponseEntity.ok("Record Updated Successfully");
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