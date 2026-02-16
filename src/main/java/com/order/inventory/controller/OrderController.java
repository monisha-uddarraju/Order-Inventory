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
    // 1) GET /api/v1/orders – Collection of Orders
    //    500 -> "An internal server error occurred while fetching all orders."
    // ---------------------------------------------------------
    @GetMapping
    public ResponseEntity<?> all() {
        try {
            // Option A: we do NOT include items; OrderDTO has no root-level items,
            // so we simply return the list as-is.
            List<OrderDTO> list = service.all();
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal server error occurred while fetching all orders.");
        }
    }

    // ---------------------------------------------------------
    // 2) POST /api/v1/orders – Create new Order
    //    400 -> "Invalid request. Please provide valid order data for creation."
    //    Success body -> "Record Created Successfully"
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<String> create(@RequestBody OrderDTO dto) {
        try {
            if (dto.getCustomerId() == null ||
                dto.getStoreId() == null ||
                dto.getStatus() == null) {
                throw new BadRequestException(
                    "Invalid request. Please provide valid order data for creation."
                );
            }
            service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Record Created Successfully");
        } catch (BadRequestException e) {
            // Force the exact Excel wording for any validation failure
            throw new BadRequestException(
                "Invalid request. Please provide valid order data for creation."
            );
        }
    }
    
 

    // ---------------------------------------------------------
    // 3) PUT /api/v1/orders – Update by object
    //    400 -> "Invalid request. Please provide valid order data for updating."
    //    Success body -> "Record Updated Successfully"
    // ---------------------------------------------------------
    @PutMapping
    public ResponseEntity<String> updateByObject(@RequestBody OrderDTO dto) {
        if (dto.getId() == null) {
            throw new BadRequestException(
                "Invalid request. Please provide valid order data for updating."
            );
        }
        try {
            service.update(dto.getId(), dto);
            return ResponseEntity.ok("Record Updated Successfully");
        } catch (BadRequestException e) {
            throw new BadRequestException(
                "Invalid request. Please provide valid order data for updating."
            );
        }
    }

//    // ---------------------------------------------------------
//    // 4) DELETE /api/v1/orders/{id}
//    //    404 -> "Order with the specified ID not found for deletion."
//    //    Success body -> "Record deleted Successfully"
//    // ---------------------------------------------------------
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> delete(@PathVariable Integer id) {
//        if (!service.exists(id)) {
//            throw new NotFoundException(
//                "Order with the specified ID not found for deletion."
//            );
//        }
//        service.delete(id);
//        return ResponseEntity.ok("Record deleted Successfully");
//    }

    // ---------------------------------------------------------
    // 5) GET /api/v1/orders/status – Count of orders by status
    //    500 -> "An internal server error occurred while fetching the count of orders by status."
    // ---------------------------------------------------------
    @GetMapping("/status")
    public ResponseEntity<?> countByStatus() {
        try {
            return ResponseEntity.ok(service.countByStatus());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An internal server error occurred while fetching the count of orders by status.");
        }
    }

    // ---------------------------------------------------------
    // 6) GET /api/v1/orders/{store} – Non-numeric store name
    //    404 -> "Orders with the specified store name not found."
    //    Response shape: orderid, orderstatus, storename, webaddress
    // ---------------------------------------------------------
    @GetMapping("/{store:^(?!\\d+$).+}")
    public ResponseEntity<?> byStoreName(@PathVariable String store) {
        List<OrderDTO> list = service.byStoreName(store);
        if (list.isEmpty()) {
            throw new NotFoundException(
                "Orders with the specified store name not found."
            );
        }

        List<Map<String, Object>> payload =
            list.stream().map(o -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("orderid", o.getId());
                m.put("orderstatus", o.getStatus());
                m.put("storename", o.getStoreName());
                m.put("webaddress", o.getWebAddress());
                return m;
            }).collect(Collectors.toList());

        return ResponseEntity.ok(payload);
    }

    // ---------------------------------------------------------
    // 7) GET /api/v1/orders/customer/{customerId}
    //    404 -> "Orders for the specified customer ID not found."
    // ---------------------------------------------------------
    @GetMapping("/customer/{customerId:\\d+}")
    public ResponseEntity<List<OrderDTO>> byCustomer(@PathVariable Integer customerId) {
        List<OrderDTO> list = service.byCustomer(customerId);
        if (list.isEmpty()) {
            throw new NotFoundException("Orders for the specified customer ID not found.");
        }
        return ResponseEntity.ok(list);
    }

    // ---------------------------------------------------------
    // 8) GET /api/v1/orders/{id}/cancel – Mark as canceled
    //    404 -> "Order with the specified ID not found for cancellation."
    //    Success -> "Success message."
    // ---------------------------------------------------------
    @GetMapping("/{id:\\d+}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Integer id) {
        if (!service.exists(id)) {
            throw new NotFoundException(
                "Order with the specified ID not found for cancellation."
            );
        }
        service.cancel(id);
        return ResponseEntity.ok("Success message.");
    }

    // ---------------------------------------------------------
    // 9) GET /api/v1/orders/{orderId} – Retrieve order by ID (single order)
    //    404 -> "Order with the specified order ID not found."
    // ---------------------------------------------------------
    @GetMapping("/{orderId:\\d+}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Integer orderId) {
        OrderDTO dto = service.get(orderId);
        return ResponseEntity.ok(dto);
    }

    // ---------------------------------------------------------
    // 10) GET /api/v1/orders/status/{status}
    //     404 -> "Orders with the specified status not found."
    // ---------------------------------------------------------
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> byStatus(@PathVariable String status) {
        List<OrderDTO> list = service.byStatus(status);
        if (list.isEmpty()) {
            throw new NotFoundException(
                "Orders with the specified status not found."
            );
        }
        return ResponseEntity.ok(list);
    }

    // ---------------------------------------------------------
    // 11) GET /api/v1/orders/date/{startDate}/{endDate}
    //     404 -> "Orders within the specified date range not found."
    // ---------------------------------------------------------
//    @GetMapping("/date/{startDate}/{endDate}")
//    public ResponseEntity<List<OrderDTO>> byDateRange(
//            @PathVariable String startDate,
//            @PathVariable String endDate) {
//
//        List<OrderDTO> list = service.byDateRange(startDate, endDate);
//        if (list.isEmpty()) {
//            throw new NotFoundException(
//                "Orders within the specified date range not found."
//            );
//        }
//        return ResponseEntity.ok(list);
//    }
    @GetMapping("/date/{startDate}/{endDate}")
    public ResponseEntity<List<OrderDTO>> byDateRange(
            @PathVariable String startDate,
            @PathVariable String endDate) {

        return ResponseEntity.ok(service.byDateRange(startDate, endDate));
    }

    // ---------------------------------------------------------
    // 12) GET /api/v1/orders/customer/{email}
    //     404 -> "Orders for the specified customer email not found."
    // ---------------------------------------------------------
    @GetMapping("/customer/{email:.+@.+}")
    public ResponseEntity<List<OrderDTO>> byCustomerEmail(@PathVariable String email) {
        List<OrderDTO> list = service.byCustomerEmail(email);
        if (list.isEmpty()) {
            throw new NotFoundException(
                "Orders for the specified customer email not found."
            );
        }
        return ResponseEntity.ok(list);
    }
}