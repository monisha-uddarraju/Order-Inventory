package com.inventory.order.service;

import com.inventory.order.dto.response.InventoryResponse;
import com.inventory.order.entity.*;
import com.inventory.order.exception.ResourceNotFoundException;
import com.inventory.order.mapper.InventoryMapper;
import com.inventory.order.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final InventoryRepository repo;
    private final StoresRepository storesRepo;
    private final ProductsRepository productsRepo;
    private final InventoryMapper mapper;

    public InventoryService(
            InventoryRepository repo,
            StoresRepository storesRepo,
            ProductsRepository productsRepo,
            InventoryMapper mapper
    ) {
        this.repo = repo;
        this.storesRepo = storesRepo;
        this.productsRepo = productsRepo;
        this.mapper = mapper;
    }

    public List<InventoryResponse> getAll() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    public List<InventoryResponse> byStore(Integer storeId) {
        return repo.findByStore_Id(storeId).stream().map(mapper::toResponse).toList();
    }

    public InventoryResponse getByStoreAndProduct(Integer storeId, Integer productId) {
        Stores s = storesRepo.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        Products p = productsRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Inventory inv = repo.findByStoreAndProduct(s, p)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory entry not found"));

        return mapper.toResponse(inv);
    }
}