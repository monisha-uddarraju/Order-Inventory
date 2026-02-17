package com.order.inventory.service;
 
import com.order.inventory.dto.InventoryDTO;

import com.order.inventory.dto.OrderDTO;

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

import java.util.stream.Collectors;
 
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

        List<InventoryDTO> list = repo.findByProductAndStore(productId, storeId)

                .stream().map(mapper::toDto).toList();

        if (list.isEmpty())

            throw new NotFoundException("Inventory records for the specified product and store not found.");

        return list;

    }
 
    /**

     * GET /api/v1/inventory/shipment (list variant)

     * Returns inventories that have matching shipments.

     * 1) Try original inventory-driven join.

     * 2) If empty, fallback: synthesize InventoryDTOs from shipped OrderItems,

     *    grouping by (store, product) and summing quantities.

     */

    public List<InventoryDTO> inventoriesWithShipments() {

        // ---- Path 1: original behavior via InventoryRepository join ----

        List<Inventory> invs = repo.findInventoriesWithAnyShipmentForTheirProduct();

        if (invs != null && !invs.isEmpty()) {

            return invs.stream().map(mapper::toDto).toList();

        }
 
        // ---- Path 2: fallback using shipped OrderItems ----

        List<OrderItem> shippedItems = itemRepo.findAllWithShipment();
 
        // Group by store + product; sum quantities

        Map<String, List<OrderItem>> byStoreProduct = shippedItems.stream()

                .filter(oi -> oi.getOrder() != null
&& oi.getOrder().getStore() != null
&& oi.getProduct() != null)

                .collect(Collectors.groupingBy(oi ->

                        oi.getOrder().getStore().getId() + "|" +

                        oi.getOrder().getStore().getStoreName() + "|" +

                        oi.getProduct().getId() + "|" +

                        oi.getProduct().getProductName()

                ));
 
        List<InventoryDTO> synthesized = new ArrayList<>();

        for (Map.Entry<String, List<OrderItem>> e : byStoreProduct.entrySet()) {

            String[] key = e.getKey().split("\\|", -1);

            Integer storeId = Integer.valueOf(key[0]);

            String storeName = key[1];

            Integer productId = Integer.valueOf(key[2]);

            String productName = key[3];
 
            int totalQty = e.getValue().stream()

                    .map(oi -> Optional.ofNullable(oi.getQuantity()).orElse(0))

                    .mapToInt(Integer::intValue)

                    .sum();
 
            // No Inventory row exists in fallback -> inventoryId = null

            synthesized.add(InventoryDTO.builder()

                    .inventoryId(null)

                    .storeId(storeId)

                    .storeName(storeName)

                    .productId(productId)

                    .productName(productName)

                    .quantity(totalQty)

                    .build());

        }
 
        return synthesized;

    }
 
    // ---------------------------------------------------------

    // /inventory/{orderid} : custom snapshot

    // ---------------------------------------------------------

    @SuppressWarnings("unchecked")

    public Map<String, Object> orderSnapshot(Integer orderId) {

        Order order = orderRepo.findById(orderId)

                .orElseThrow(() -> new NotFoundException(

                        "Store, product, and customer data for the specified order ID not found."));
 
        List<OrderItem> items = itemRepo.findByOrderId(orderId);

        if (items.isEmpty()) {

            throw new NotFoundException(

                    "Store, product, and customer data for the specified order ID not found.");

        }
 
        Map<String, Object> customer = Map.of(

                "id", order.getCustomer().getId(),

                "fullName", order.getCustomer().getFullName(),

                "email", order.getCustomer().getEmailAddress()

        );
 
        Map<String, Object> store = Map.of(

                "id", order.getStore().getId(),

                "storeName", order.getStore().getStoreName(),

                "webAddress", order.getStore().getWebAddress()

        );
 
        List<Map<String, Object>> products = items.stream().map(i -> {

            Map<String, Object> m = new LinkedHashMap<>();

            m.put("productId", i.getProduct().getId());

            m.put("productName", i.getProduct().getProductName());

            m.put("unitPrice", i.getUnitPrice());

            m.put("quantity", i.getQuantity());

            m.put("shipmentStatus", i.getShipment() != null

                    ? i.getShipment().getShipmentStatus().name()

                    : null);

            return m;

        }).collect(Collectors.toList());
 
        return Map.of(

                "orderId", orderId,

                "orderStatus", String.valueOf(order.getOrderStatus()),

                "customer", customer,

                "store", store,

                "products", products

        );

    }
 
    // ---------------------------------------------------------

    // /inventory/category/{category} : treat as brand OR colour

    // ---------------------------------------------------------

    public List<InventoryDTO> byCategoryRequired(String category) {

        final String cat = category == null ? "" : category.trim();
 
        List<InventoryDTO> list = repo.findAll().stream()

                .filter(i -> i.getProduct() != null
&& ((i.getProduct().getBrand() != null
&& i.getProduct().getBrand().equalsIgnoreCase(cat))

                         || (i.getProduct().getColour() != null
&& i.getProduct().getColour().equalsIgnoreCase(cat))))

                .map(mapper::toDto)

                .toList();
 
        if (list.isEmpty())

            throw new NotFoundException("Inventory records for the specified category not found.");

        return list;

    }

}
 