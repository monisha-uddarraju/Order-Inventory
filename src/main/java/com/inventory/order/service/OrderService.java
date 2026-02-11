package com.inventory.order.service;

import com.inventory.order.dto.request.CreateOrderRequest;
import com.inventory.order.dto.response.OrderResponse;
import com.inventory.order.entity.*;
import com.inventory.order.exception.*;
import com.inventory.order.mapper.OrderMapper;
import com.inventory.order.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrdersRepository ordersRepo;
    private final CustomersRepository customersRepo;
    private final StoresRepository storesRepo;
    private final ProductsRepository productsRepo;
    private final InventoryRepository inventoryRepo;
    private final OrderMapper mapper;

    public OrderService(
            OrdersRepository ordersRepo,
            CustomersRepository customersRepo,
            StoresRepository storesRepo,
            ProductsRepository productsRepo,
            InventoryRepository inventoryRepo,
            OrderMapper mapper
    ) {
        this.ordersRepo = ordersRepo;
        this.customersRepo = customersRepo;
        this.storesRepo = storesRepo;
        this.productsRepo = productsRepo;
        this.inventoryRepo = inventoryRepo;
        this.mapper = mapper;
    }

    public List<OrderResponse> getAll() {
        return ordersRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    public OrderResponse create(CreateOrderRequest req) {
        Customers c = customersRepo.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Stores s = storesRepo.findById(req.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        Orders o = Orders.builder()
                .customer(c)
                .store(s)
                .orderStatus("Pending")
                .orderTms(Instant.now())
                .build();

        ordersRepo.save(o);

        int line = 1;
        for (var item : req.items()) {
            Products p = productsRepo.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            Inventory inv = inventoryRepo.findByStoreAndProduct(s, p)
                    .orElseThrow(() -> new BadRequestException("Stock not available"));

            if (inv.getProductInventory() < item.quantity())
                throw new BadRequestException("Insufficient stock");

            inv.setProductInventory(inv.getProductInventory() - item.quantity());

            Order_Items oi = Order_Items.builder()
                    .order(o)
                    .lineItemId(line++)
                    .product(p)
                    .unitPrice(p.getUnitPrice())
                    .quantity(item.quantity())
                    .build();
            o.getItems().add(oi);
        }

        return mapper.toResponse(o);
    }

    public OrderResponse get(Integer id) {
        Orders o = ordersRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapper.toResponse(o);
    }

    public void delete(Integer id) {
        if (!ordersRepo.existsById(id))
            throw new ResourceNotFoundException("Order not found");
        ordersRepo.deleteById(id);
    }
}