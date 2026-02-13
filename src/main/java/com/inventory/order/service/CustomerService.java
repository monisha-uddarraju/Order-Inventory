package com.inventory.order.service;

import com.inventory.order.dto.request.*;
import com.inventory.order.dto.response.CustomerResponse;
import com.inventory.order.entity.Customers;
import com.inventory.order.exception.ResourceNotFoundException;
import com.inventory.order.mapper.CustomerMapper;
import com.inventory.order.repository.CustomersRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomersRepository repo;
    private final CustomerMapper mapper;

    public CustomerService(CustomersRepository repo, CustomerMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public List<CustomerResponse> getAll() {
        return repo.findAll().stream().map(mapper::toResponse).toList();
    }

    public CustomerResponse create(CreateCustomerRequest req) {
        Customers c = mapper.fromCreateRequest(req);
        return mapper.toResponse(repo.save(c));
    }

    public CustomerResponse update(UpdateCustomerRequest req) {
        Customers c = repo.findById(req.id())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        c.setEmailAddress(req.emailAddress());
        c.setFullName(req.fullName());
        return mapper.toResponse(c);
    }

    public void delete(Integer id) {
        if (!repo.existsById(id))
            throw new ResourceNotFoundException("Customer not found");
        repo.deleteById(id);
    }

    public CustomerResponse findByEmail(String email) {
        Customers c = repo.findByEmailAddress(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));
        return mapper.toResponse(c);
    }

    public List<CustomerResponse> findByName(String name) {
        return repo.findByFullNameContainingIgnoreCase(name).stream()
                .map(mapper::toResponse).toList();
    }
}//1