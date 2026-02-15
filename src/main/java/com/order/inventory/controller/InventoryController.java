package com.order.inventory.controller;

import com.order.inventory.dto.InventoryDTO;

import com.order.inventory.dto.OrderDTO;
import com.order.inventory.dto.ShipmentDTO;
import com.order.inventory.entity.Inventory;
import com.order.inventory.entity.OrderItem;
import com.order.inventory.dto.OrderItemDTO;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.service.InventoryService;
import com.order.inventory.service.OrderService;
import com.order.inventory.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryService invService;
	private final ShipmentService shipService;
	private final OrderService orderService;

	/**
	 * CSV: 1) GET /api/v1/inventory – all inventory 2) GET
	 * /api/v1/inventory?storeid=value – filter by store (404 when none)
	 */
	@GetMapping
	public ResponseEntity<List<InventoryDTO>> all(@RequestParam(required = false, name = "storeid") Integer storeId) {
		if (storeId != null)
			return ResponseEntity.ok(invService.byStoreRequired(storeId));
		return ResponseEntity.ok(invService.all());
	}

	/**
	 * CSV: GET /api/v1/inventory/shipment - "Fetch inventories and matching
	 * shipments" (default) - "Count shipment status wise count of total products
	 * sold" (when aggregate=true)
	 */
	@GetMapping("/shipment/count-by-status")
	public ResponseEntity<?> shipmentEither(
			@RequestParam(name = "aggregate", required = false, defaultValue = "false") boolean aggregate) {
		if (aggregate) {
			// status -> totalSold
			List<ShipmentDTO.SoldCount> payload = shipService.totalSoldGroupedByShipmentStatus();
			return ResponseEntity.ok(payload);
		}
		// list of inventories that have shipments
		return ResponseEntity.ok(invService.inventoriesWithShipments());
	}

	/**
	 * CSV: GET /api/v1/inventory/{orderid} Return a custom object with customer,
	 * store, and product data for the order (404 if none).
	 */
	@GetMapping("/{orderid}")
	public ResponseEntity<Map<String, Object>> snapshotByOrder(@PathVariable("orderid") Integer orderId) {
		return ResponseEntity.ok(invService.orderSnapshot(orderId));
	}

	/**
	 * CSV: GET /api/v1/inventory/{orderid}/details Products in order + storeName +
	 * shipmentStatus + total amount (uses existing OrderService).
	 */
	@GetMapping("/{orderid}/details")
	public ResponseEntity<OrderDTO.Details> orderDetails(@PathVariable("orderid") Integer orderId) {
		OrderDTO.Details details = orderService.details(orderId);
		// If you want to enforce 404 when no items present, uncomment:
		// if (details.getItems() == null || details.getItems().isEmpty())
		// throw new NotFoundException("List of products in the specified order ID not
		// found with store details, shipment status, and total amount.");
		return ResponseEntity.ok(details);
	}

	/**
	 * CSV: GET /api/v1/inventory/product/{productId}/store/{storeId} 404 when none.
	 */
	@GetMapping("/product/{productId}/store/{storeId}")
	public ResponseEntity<List<InventoryDTO>> byProductAndStore(@PathVariable Integer productId,
			@PathVariable Integer storeId) {
		return ResponseEntity.ok(invService.byProductAndStoreRequired(productId, storeId));
	}

	/**
	 * CSV: GET /api/v1/inventory/category/{category} Since there's no "category"
	 * column, we treat category as brand or colour. 404 when none.
	 */
	@GetMapping("/category/{category}")
	public ResponseEntity<List<InventoryDTO>> byCategory(@PathVariable String category) {
		return ResponseEntity.ok(invService.byCategoryRequired(category));
	}

	// GET /api/v1/inventory or /api/v1/inventory?storeid=value
	@GetMapping("/storeId/{storeId}")
	public ResponseEntity<List<InventoryDTO>> get(@RequestParam(value = "storeid", required = false) Integer storeId) {
		if (storeId == null)
			return ResponseEntity.ok(invService.all());
		return ResponseEntity.ok(invService.byStoreRequired(storeId));
	}
	

	 @GetMapping("/shipments")
	    public ResponseEntity<List<InventoryDTO>> inventoriesWithShipments() {
	        return ResponseEntity.ok(invService.inventoriesWithShipments());
	    }


	

}