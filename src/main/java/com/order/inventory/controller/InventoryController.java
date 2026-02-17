package com.order.inventory.controller;

import com.order.inventory.dto.InventoryDTO;

import com.order.inventory.dto.OrderDTO;

import com.order.inventory.dto.ShipmentDTO;

import com.order.inventory.exception.NotFoundException;

import com.order.inventory.service.InventoryService;

import com.order.inventory.service.OrderService;

import com.order.inventory.service.ShipmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController

@RequestMapping("/api/v1/inventory")

@RequiredArgsConstructor

public class InventoryController {

    private final InventoryService invService;

    private final ShipmentService shipService;

    private final OrderService orderService;

    // 1) GET /api/v1/inventory

    // 2) GET /api/v1/inventory?storeid=value

    @GetMapping

    public ResponseEntity<?> all(@RequestParam(required = false, name = "storeid") Integer storeId) {

        if (storeId != null) {

            // CSV: Custom object of product, store, order details

            List<InventoryDTO> list = invService.byStoreRequired(storeId);

            Map<String, Object> payload = new LinkedHashMap<>();

            payload.put("storeId", storeId);

            // Build store block (using first record)

            if (!list.isEmpty()) {

                InventoryDTO first = list.get(0);

                Map<String, Object> store = Map.of(

                        "storeId", first.getStoreId(),

                        "storeName", first.getStoreName()

                );

                payload.put("store", store);

            }

            // Product list

            List<Map<String, Object>> products = new ArrayList<>();

            for (InventoryDTO dto : list) {

                products.add(Map.of(

                        "productId", dto.getProductId(),

                        "productName", dto.getProductName(),

                        "quantity", dto.getQuantity()

                ));

            }

            payload.put("products", products);

            return ResponseEntity.ok(payload);

        }

        // CSV: Must return a custom object (NOT bare list)

        List<InventoryDTO> all = invService.all();

        Map<String, Object> payload = Map.of("inventories", all);

        return ResponseEntity.ok(payload);

    }

    // 3) GET /api/v1/inventory/shipment  (fetch inventories + shipments)

    @GetMapping(value = "/shipment", params = "!aggregate")

    public ResponseEntity<?> inventoriesWithShipments_list() {

        return ResponseEntity.ok(invService.inventoriesWithShipments());

    }

    // 5) GET /api/v1/inventory/shipment?aggregate=true (count shipment status wise)

    @GetMapping(value = "/shipment", params = "aggregate=true")

    public ResponseEntity<?> inventoriesWithShipments_count() {

        List<ShipmentDTO.SoldCount> rows = shipService.totalSoldGroupedByShipmentStatus();

        Map<String, Object> payload = Map.of("shipmentSoldCount", rows);

        return ResponseEntity.ok(payload);

    }

    // 4) GET /api/v1/inventory/{orderid}

    @GetMapping("/{orderid}")

    public ResponseEntity<Map<String, Object>> snapshotByOrder(@PathVariable("orderid") Integer orderId) {

        return ResponseEntity.ok(invService.orderSnapshot(orderId));

    }

    // 6) GET /api/v1/inventory/{orderid}/details

    @GetMapping("/{orderid}/details")

    public ResponseEntity<OrderDTO.Details> orderDetails(@PathVariable("orderid") Integer orderId) {

        OrderDTO.Details details = orderService.details(orderId);

        // CSV requires 404 when list empty

        if (details.getItems() == null || details.getItems().isEmpty()) {

            throw new NotFoundException(

                    "List of products in the specified order ID not found with store details, shipment status, and total amount."

            );

        }

        return ResponseEntity.ok(details);

    }

    // 7) GET /api/v1/inventory/product/{productId}/store/{storeId}

    @GetMapping("/product/{productId}/store/{storeId}")

    public ResponseEntity<List<InventoryDTO>> byProductAndStore(@PathVariable Integer productId,

                                                                @PathVariable Integer storeId) {

        return ResponseEntity.ok(invService.byProductAndStoreRequired(productId, storeId));

    }

    // 8) GET /api/v1/inventory/category/{category}

//    @GetMapping("/category/{category}")

//    public ResponseEntity<List<InventoryDTO>> byCategory(@PathVariable String category) {

//        return ResponseEntity.ok(invService.byCategoryRequired(category));

//    }

    @GetMapping("/category/{category}")

	public ResponseEntity<List<InventoryDTO>> byCategory(@PathVariable String category) {

		return ResponseEntity.ok(invService.byCategoryRequired(category));

	}

}

 