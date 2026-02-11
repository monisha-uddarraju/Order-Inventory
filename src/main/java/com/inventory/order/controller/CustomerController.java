package com.inventory.order.controller;

import com.inventory.order.dto.request.*;
import com.inventory.order.dto.response.CustomerResponse;
import com.inventory.order.service.CustomerService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;
    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public List<CustomerResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest req) {
        return service.create(req);
    }

    @PutMapping
    public CustomerResponse update(@Valid @RequestBody UpdateCustomerRequest req) {
        return service.update(req);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "Customer deleted";
    }

    @GetMapping("/email/{email}")
    public CustomerResponse byEmail(@PathVariable String email) {
        return service.findByEmail(email);
    }

    @GetMapping("/name/{name}")
    public List<CustomerResponse> byName(@PathVariable String name) {
        return service.findByName(name);
    }
}