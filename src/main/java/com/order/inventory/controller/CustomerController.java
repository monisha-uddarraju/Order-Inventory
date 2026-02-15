package com.order.inventory.controller;

import com.order.inventory.dto.CustomerDTO;
import com.order.inventory.dto.OrderDTO;
import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.service.CustomerService;
import com.order.inventory.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;
    private final OrderService orderService;

    // ---------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------

    // GET /api/v1/customers  – Fetch all customers
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> all() {
        return ResponseEntity.ok(service.all());
    }

    // POST /api/v1/customers – Add new customer
    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody CustomerDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    // PUT /api/v1/customers – Update by object (CSV requires this form)
    @PutMapping
    public ResponseEntity<CustomerDTO> updateByObject(@RequestBody CustomerDTO dto) {
        if (dto.getId() == null) throw new BadRequestException("Customer id is required for update");
        return ResponseEntity.ok(service.update(dto.getId(), dto));
    }

//    // (Kept too for RESTful style) PUT /api/v1/customers/{id}
//    @PutMapping("/{id}")
//    public ResponseEntity<CustomerDTO> update(@PathVariable Integer id, @RequestBody CustomerDTO dto) {
//        return ResponseEntity.ok(service.update(id, dto));
//    }

//    // CSV shows a closing parenthesis typo; we implement the correct brace: {customerId}
//    // DELETE /api/v1/customers/{customerId} – Delete
//    @DeleteMapping("/{customerId}")
//    public ResponseEntity<Void> delete(@PathVariable Integer customerId) {
//        service.delete(customerId);
//        return ResponseEntity.noContent().build();
//    }

    // ---------------------------------------------------------
    // Search (email / name)
    // ---------------------------------------------------------

    // Safer/explicit routes first (good for Swagger clarity):
    // GET /api/v1/customers/email/{emailId}
    @GetMapping("/email/{emailId}")
    public ResponseEntity<List<CustomerDTO>> byEmailExplicit(@PathVariable String emailId) {
        return ResponseEntity.ok(service.byEmailRequired(emailId));
    }

    // GET /api/v1/customers/name/{name}
    @GetMapping("/name/{name}")
    public ResponseEntity<List<CustomerDTO>> byNameExplicit(@PathVariable String name) {
        return ResponseEntity.ok(service.byNameWildcardRequired(name));
    }

    // CSV single-segment ambiguous forms (kept with regex; email contains '@'):
    // GET /api/v1/customers/{emailId}
    @GetMapping("/{emailId:.+@.+}")
    public ResponseEntity<List<CustomerDTO>> byEmailLegacy(@PathVariable String emailId) {
        return ResponseEntity.ok(service.byEmailRequired(emailId));
    }

    // GET /api/v1/customers/{name}
    @GetMapping("/{name:^(?!.*@).+$}")
    public ResponseEntity<List<CustomerDTO>> byNameLegacy(@PathVariable String name) {
        return ResponseEntity.ok(service.byNameWildcardRequired(name));
    }

    // ---------------------------------------------------------
    // Shipment status wise customer count
    // ---------------------------------------------------------

    // GET /api/v1/customers/shipment/status
    @GetMapping("/shipment/status")
    public ResponseEntity<Map<String, Long>> shipmentStatusWiseCustomerCount() {
        return ResponseEntity.ok(service.shipmentStatusWiseCustomerCount());
    }

    // ---------------------------------------------------------
    // Customer's orders & shipments
    // ---------------------------------------------------------

    // GET /api/v1/customers/{custId}/order – fetch orders for the customer
    @GetMapping("/{custId}/order")
    public ResponseEntity<List<OrderDTO>> customerOrders(@PathVariable Integer custId) {
        List<OrderDTO> orders = orderService.byCustomerRequired(custId);
        return ResponseEntity.ok(orders);
    }

    // GET /api/v1/customers/{custId}/shipment – fetch shipment history for the customer
    @GetMapping("/{custId}/shipment")
    public ResponseEntity<List<ShipmentDTO>> customerShipments(@PathVariable Integer custId) {
        return ResponseEntity.ok(service.shipmentsByCustomerRequired(custId));
    }

    // ---------------------------------------------------------
    // Derived filters required by CSV
    // ---------------------------------------------------------

    // GET /api/v1/customers/shipments/pending – customers with pending shipments
    @GetMapping("/shipments/pending")
    public ResponseEntity<List<CustomerDTO>> customersWithPendingShipments() {
        return ResponseEntity.ok(service.customersByShipmentStatus(ShipmentStatus.PENDING, true));
    }

    // GET /api/v1/customers/shipments/overdue – customers with overdue shipments
    @GetMapping("/shipments/overdue")
    public ResponseEntity<List<CustomerDTO>> customersWithOverdueShipments() {
        return ResponseEntity.ok(service.customersByShipmentStatus(ShipmentStatus.OVERDUE, true));
    }

    // GET /api/v1/customers/orders/completed – customers with completed orders
    @GetMapping("/orders/completed")
    public ResponseEntity<List<CustomerDTO>> customersWithCompletedOrders() {
        return ResponseEntity.ok(service.customersWithCompletedOrdersRequired());
    }

    // GET /api/v1/customers/orders/quantity/{min}/{max}
    @GetMapping("/orders/quantity/{min}/{max}")
    public ResponseEntity<List<CustomerDTO>> customersByOrderQty(@PathVariable long min, @PathVariable long max) {
        return ResponseEntity.ok(service.customersByOrderQuantityBetweenRequired(min, max));
    }
}