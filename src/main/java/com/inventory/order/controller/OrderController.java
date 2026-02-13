package com.inventory.order.controller;

import com.inventory.order.dto.request.CreateOrderRequest;
import com.inventory.order.dto.response.OrderResponse;
import com.inventory.order.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @GetMapping
    public List<OrderResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Integer id) {
        return service.get(id);
    }

//    @DeleteMapping("/{id}")
//    public String delete(@PathVariable Integer id) {
//        service.delete(id);
//        return "Order deleted";
//    }
}
//4