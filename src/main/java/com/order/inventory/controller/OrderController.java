package com.order.inventory.controller;

import com.order.inventory.dto.OrderDTO;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    // ---------------------------------------------------------
    // CRUD + base reads
    // ---------------------------------------------------------

    /** CSV: GET /api/v1/orders – Fetch all orders */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> all() {
        return ResponseEntity.ok(service.all());
    }

    /** CSV: POST /api/v1/orders – Create new Order (400 on invalid) */
    @PostMapping
    public ResponseEntity<OrderDTO> create(@RequestBody OrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /** CSV: PUT /api/v1/orders – Update order by object (id is required in body) */
    @PutMapping
    public ResponseEntity<OrderDTO> updateByObject(@RequestBody OrderDTO dto) {
        if (dto.getId() == null) throw new BadRequestException("Order id is required for update");
        return ResponseEntity.ok(service.update(dto.getId(), dto));
    }
//
//    /** (Kept too for RESTful style) PUT /api/v1/orders/{id} */
//    @PutMapping("/{id}")
//    public ResponseEntity<OrderDTO> update(@PathVariable Integer id, @RequestBody OrderDTO dto) {
//        return ResponseEntity.ok(service.update(id, dto));
//    }

//    /** CSV: DELETE /api/v1/orders/{id} – Delete Order (404 if not found) */
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Integer id) {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }

    // ---------------------------------------------------------
    // Status count + list by status
    // ---------------------------------------------------------

    /** CSV: GET /api/v1/orders/status – Count of orders by status */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Long>> countByStatus() {
        return ResponseEntity.ok(service.countByStatus());
    }

    /** CSV: GET /api/v1/orders/status/{status} – Retrieve orders by status (404 if none) */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> byStatus(@PathVariable String status) {
        List<OrderDTO> out = service.byStatus(status);
        if (out.isEmpty()) {
            throw new NotFoundException("Orders with the specified status not found.");
        }
        return ResponseEntity.ok(out);
    }

    // ---------------------------------------------------------
    // Get by id + alias (to reflect both CSV rows)
    // ---------------------------------------------------------

    /** CSV: GET /api/v1/orders/{id} – numeric id only */
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<OrderDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.get(id));
    }

    /** CSV: GET /api/v1/orders/{orderId} – alternate row; we expose as /id/{orderId} to avoid path conflicts */
    @GetMapping("/id/{orderId:\\d+}")
    public ResponseEntity<OrderDTO> getByOrderIdAlias(@PathVariable Integer orderId) {
        return ResponseEntity.ok(service.get(orderId));
    }

    // ---------------------------------------------------------
    // Cancel
    // ---------------------------------------------------------

    /** CSV: GET /api/v1/orders/{id}/cancel – Mark an order as canceled (404 if not found) */
    @GetMapping("/{id:\\d+}/cancel")
    public ResponseEntity<OrderDTO> cancel(@PathVariable Integer id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    // ---------------------------------------------------------
    // Customer filters (id / email)
    // ---------------------------------------------------------

    /** CSV: GET /api/v1/orders/customer/{customerId} – numeric; 404 if none */
    @GetMapping("/customer/{customerId:\\d+}")
    public ResponseEntity<List<OrderDTO>> byCustomer(@PathVariable Integer customerId) {
        List<OrderDTO> out = service.byCustomer(customerId);
        if (out.isEmpty()) {
            throw new NotFoundException("Orders for the specified customer ID not found.");
        }
        return ResponseEntity.ok(out);
    }

    /** CSV: GET /api/v1/orders/customer/{email} – email pattern; 404 if none */
    @GetMapping("/customer/{email:.+@.+}")
    public ResponseEntity<List<OrderDTO>> byCustomerEmail(@PathVariable String email) {
        List<OrderDTO> out = service.byCustomerEmail(email);
        if (out.isEmpty()) {
            throw new NotFoundException("Orders for the specified customer email not found.");
        }
        return ResponseEntity.ok(out);
    }

    // ---------------------------------------------------------
    // Store filter (avoid clash with numeric id)
    // ---------------------------------------------------------

    /**
     * CSV: GET /api/v1/orders/{store} – store name (non-numeric), with shape:
     *  - orderid, orderstatus, storename, webaddress
     * Returns 404 if none.
     */
    @GetMapping("/{store:^(?!\\d+$).+}")
    public ResponseEntity<List<Map<String, Object>>> byStoreName(@PathVariable String store) {
        List<OrderDTO> list = service.byStoreName(store);
        if (list.isEmpty()) {
            throw new NotFoundException("Orders with the specified store name not found.");
        }
        // Shape: orderid, orderstatus, storename, webaddress (real values from DTO)
        List<Map<String, Object>> payload = list.stream()
                .map(o -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("orderid", o.getId());
                    m.put("orderstatus", o.getStatus());
                    m.put("storename", o.getStoreName());
                    m.put("webaddress", o.getWebAddress());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(payload);
    }

    // ---------------------------------------------------------
    // Date range
    // ---------------------------------------------------------

    /** CSV: GET /api/v1/orders/date/{startDate}/{endDate} – yyyy-MM-dd; 404 if none */
    @GetMapping("/date/{startDate}/{endDate}")
    public ResponseEntity<List<OrderDTO>> byDateRange(@PathVariable String startDate, @PathVariable String endDate) {
        List<OrderDTO> out = service.byDateRange(startDate, endDate);
        if (out.isEmpty()) {
            throw new NotFoundException("Orders within the specified date range not found.");
        }
        return ResponseEntity.ok(out);
    }
}