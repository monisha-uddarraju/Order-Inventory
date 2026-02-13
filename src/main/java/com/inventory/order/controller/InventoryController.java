package com.inventory.order.controller;

import com.inventory.order.dto.response.InventoryResponse;
import com.inventory.order.service.InventoryService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService service;
    public InventoryController(InventoryService service) { this.service = service; }

    @GetMapping
    public List<InventoryResponse> getAll(@RequestParam(required = false) Integer storeId) {
        return (storeId == null) ? service.getAll() : service.byStore(storeId);
    }

    @GetMapping("/product/{productId}/store/{storeId}")
    public InventoryResponse getByStoreAndProduct(
            @PathVariable Integer productId,
            @PathVariable Integer storeId) {
        return service.getByStoreAndProduct(storeId, productId);
    }
}
//3