package com.order.inventory.service;

import com.order.inventory.dto.InventoryDTO;
import com.order.inventory.dto.OrderDTO;
import com.order.inventory.dto.OrderDTO.LineItem;
import com.order.inventory.dto.OrderItemDTO;
import com.order.inventory.entity.Inventory;
import com.order.inventory.entity.Order;
import com.order.inventory.entity.OrderItem;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.InventoryMapper;
import com.order.inventory.repository.InventoryRepository;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

	private final InventoryRepository repo;
	private final InventoryMapper mapper;

	// For building /inventory/{orderid} snapshot
	private final OrderRepository orderRepo;
	private final OrderItemRepository itemRepo;

	// ---------------------------------------------------------
	// Basic fetches
	// ---------------------------------------------------------

	public List<InventoryDTO> all() {
		return repo.findAll().stream().map(mapper::toDto).toList();
	}

	public List<InventoryDTO> byStoreRequired(Integer storeId) {
		List<InventoryDTO> list = repo.findByStoreId(storeId).stream().map(mapper::toDto).toList();
		if (list.isEmpty())
			throw new NotFoundException("Inventory records matching the specified store ID not found.");
		return list;
	}

	public List<InventoryDTO> byProductAndStoreRequired(Integer productId, Integer storeId) {
		List<InventoryDTO> list = repo.findByProductAndStore(productId, storeId).stream().map(mapper::toDto).toList();
		if (list.isEmpty())
			throw new NotFoundException("Inventory records for the specified product and store not found.");
		return list;
	}

	public List<InventoryDTO> inventoriesWithShipments() {
		return repo.findInventoriesWithAnyShipmentForTheirProduct().stream().map(mapper::toDto).toList();
	}

	// ---------------------------------------------------------
	// /inventory/{orderid} : custom snapshot
	// ---------------------------------------------------------
	@SuppressWarnings("unchecked")
	public Map<String, Object> orderSnapshot(Integer orderId) {
		Order order = orderRepo.findById(orderId).orElseThrow(
				() -> new NotFoundException("Store, product, and customer data for the specified order ID not found."));

		List<OrderItem> items = itemRepo.findByOrderId(orderId);
		if (items.isEmpty()) {
			throw new NotFoundException("Store, product, and customer data for the specified order ID not found.");
		}

		Map<String, Object> customer = Map.of("id", order.getCustomer().getId(), "fullName",
				order.getCustomer().getFullName(), "email", order.getCustomer().getEmailAddress());

		Map<String, Object> store = Map.of("id", order.getStore().getId(), "storeName", order.getStore().getStoreName(),
				"webAddress", order.getStore().getWebAddress());

		// Replace the products creation block in InventoryService.orderSnapshot(...)
		// with this:
		List<Map<String, Object>> products = items.stream().map(i -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("productId", i.getProduct().getId());
			m.put("productName", i.getProduct().getProductName());
			m.put("unitPrice", i.getUnitPrice());
			m.put("quantity", i.getQuantity());
			m.put("shipmentStatus", i.getShipment() != null ? i.getShipment().getShipmentStatus().name() : null);
			return m;
		}).collect(java.util.stream.Collectors.toList());

		return Map.of("orderId", orderId, "orderStatus", String.valueOf(order.getOrderStatus()), "customer", customer,
				"store", store, "products", products);
	}

	// ---------------------------------------------------------
	// /inventory/category/{category} : treat as brand OR colour
	// ---------------------------------------------------------
	public List<InventoryDTO> byCategoryRequired(String category) {
		final String cat = category == null ? "" : category.trim();
		List<InventoryDTO> list = repo.findAll().stream().filter(i -> i.getProduct() != null
				&& ((i.getProduct().getBrand() != null && i.getProduct().getBrand().equalsIgnoreCase(cat))
						|| (i.getProduct().getColour() != null && i.getProduct().getColour().equalsIgnoreCase(cat))))
				.map(mapper::toDto).toList();

		if (list.isEmpty())
			throw new NotFoundException("Inventory records for the specified category not found.");
		return list;
	}

	
}