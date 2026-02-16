package com.inventory.service;
import com.order.inventory.dto.InventoryDTO;
import com.order.inventory.entity.Inventory;
import com.order.inventory.entity.Order;
import com.order.inventory.entity.OrderItem;
import com.order.inventory.entity.OrderStatus;
import com.order.inventory.entity.Product;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.Store;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.InventoryMapper;
import com.order.inventory.repository.InventoryRepository;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.OrderRepository;
import com.order.inventory.service.InventoryService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
   @Mock
   private InventoryRepository repo;
   @Mock
   private InventoryMapper mapper;
   @Mock
   private OrderRepository orderRepo;
   @Mock
   private OrderItemRepository itemRepo;
   @InjectMocks
   private InventoryService service;
   // ---------------------------------------------------------
   // all()
   // ---------------------------------------------------------
   @Test
   @DisplayName("all() returns mapped list")
   void all_returnsMappedList() {
       Inventory inv1 = mock(Inventory.class);
       Inventory inv2 = mock(Inventory.class);
       InventoryDTO dto1 = InventoryDTO.builder().inventoryId(1).storeId(10).productId(100).quantity(5).build();
       InventoryDTO dto2 = InventoryDTO.builder().inventoryId(2).storeId(20).productId(200).quantity(15).build();
       when(repo.findAll()).thenReturn(List.of(inv1, inv2));
       when(mapper.toDto(inv1)).thenReturn(dto1);
       when(mapper.toDto(inv2)).thenReturn(dto2);
       List<InventoryDTO> out = service.all();
       assertEquals(2, out.size());
       assertEquals(1, out.get(0).getInventoryId());
       assertEquals(2, out.get(1).getInventoryId());
       verify(repo, times(1)).findAll();
       verify(mapper, times(2)).toDto(any(Inventory.class));
   }
   // ---------------------------------------------------------
   // byStoreRequired(Integer)
   // ---------------------------------------------------------
   @Test
   @DisplayName("byStoreRequired() returns mapped list when found")
   void byStoreRequired_found() {
       Inventory inv = mock(Inventory.class);
       InventoryDTO dto = InventoryDTO.builder().inventoryId(3).storeId(11).productId(101).quantity(7).build();
       when(repo.findByStoreId(11)).thenReturn(List.of(inv));
       when(mapper.toDto(inv)).thenReturn(dto);
       List<InventoryDTO> out = service.byStoreRequired(11);
       assertEquals(1, out.size());
       assertEquals(3, out.get(0).getInventoryId());
       assertEquals(11, out.get(0).getStoreId());
   }
   @Test
   @DisplayName("byStoreRequired() throws NotFound when empty")
   void byStoreRequired_empty() {
       when(repo.findByStoreId(99)).thenReturn(List.of());
       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byStoreRequired(99));
       assertEquals("Inventory records matching the specified store ID not found.", ex.getMessage());
   }
   // ---------------------------------------------------------
   // byProductAndStoreRequired(Integer, Integer)
   // ---------------------------------------------------------
   @Test
   @DisplayName("byProductAndStoreRequired() returns mapped list when found")
   void byProductAndStoreRequired_found() {
       Inventory inv = mock(Inventory.class);
       InventoryDTO dto = InventoryDTO.builder().inventoryId(4).storeId(12).productId(102).quantity(9).build();
       when(repo.findByProductAndStore(102, 12)).thenReturn(List.of(inv));
       when(mapper.toDto(inv)).thenReturn(dto);
       List<InventoryDTO> out = service.byProductAndStoreRequired(102, 12);
       assertEquals(1, out.size());
       assertEquals(4, out.get(0).getInventoryId());
       assertEquals(102, out.get(0).getProductId());
       assertEquals(12, out.get(0).getStoreId());
   }
   @Test
   @DisplayName("byProductAndStoreRequired() throws NotFound when empty")
   void byProductAndStoreRequired_empty() {
       when(repo.findByProductAndStore(1, 2)).thenReturn(List.of());
       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byProductAndStoreRequired(1, 2));
       assertEquals("Inventory records for the specified product and store not found.", ex.getMessage());
   }
   // ---------------------------------------------------------
   // inventoriesWithShipments()
   // ---------------------------------------------------------
   @Test
   @DisplayName("inventoriesWithShipments() returns mapped list")
   void inventoriesWithShipments_returnsMappedList() {
       Inventory inv1 = mock(Inventory.class);
       Inventory inv2 = mock(Inventory.class);
       InventoryDTO dto1 = InventoryDTO.builder().inventoryId(5).build();
       InventoryDTO dto2 = InventoryDTO.builder().inventoryId(6).build();
       when(repo.findInventoriesWithAnyShipmentForTheirProduct()).thenReturn(List.of(inv1, inv2));
       when(mapper.toDto(inv1)).thenReturn(dto1);
       when(mapper.toDto(inv2)).thenReturn(dto2);
       List<InventoryDTO> out = service.inventoriesWithShipments();
       assertEquals(2, out.size());
       assertEquals(5, out.get(0).getInventoryId());
       assertEquals(6, out.get(1).getInventoryId());
   }
   // ---------------------------------------------------------
   // orderSnapshot(Integer)
   // ---------------------------------------------------------
   @Test
   @DisplayName("orderSnapshot() builds snapshot map from order and items")
   void orderSnapshot_success() {
       Integer orderId = 77;
       // Order and nested entities
       Order order = mock(Order.class);
       com.order.inventory.entity.Customer cust = mock(com.order.inventory.entity.Customer.class);
       Store store = mock(Store.class);
       when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
       when(order.getCustomer()).thenReturn(cust);
       when(order.getStore()).thenReturn(store);
       when(order.getOrderStatus()).thenReturn(OrderStatus.NEW);
       when(cust.getId()).thenReturn(1001);
       when(cust.getFullName()).thenReturn("Alice W");
       when(cust.getEmailAddress()).thenReturn("alice@example.com");
       when(store.getId()).thenReturn(2001);
       when(store.getStoreName()).thenReturn("Main Store");
       when(store.getWebAddress()).thenReturn("https://store.test");
       // Items
       OrderItem item1 = mock(OrderItem.class);
       OrderItem item2 = mock(OrderItem.class);
       Product p1 = mock(Product.class);
       Product p2 = mock(Product.class);
       Shipment sh1 = mock(Shipment.class);
       when(itemRepo.findByOrderId(orderId)).thenReturn(List.of(item1, item2));
       when(item1.getProduct()).thenReturn(p1);
       when(p1.getId()).thenReturn(301);
       when(p1.getProductName()).thenReturn("Phone");
       when(item1.getUnitPrice()).thenReturn(new BigDecimal("499.99"));
       when(item1.getQuantity()).thenReturn(2);
       when(item1.getShipment()).thenReturn(sh1);
       when(sh1.getShipmentStatus()).thenReturn(com.order.inventory.entity.ShipmentStatus.PENDING);
       when(item2.getProduct()).thenReturn(p2);
       when(p2.getId()).thenReturn(302);
       when(p2.getProductName()).thenReturn("Case");
       when(item2.getUnitPrice()).thenReturn(new BigDecimal("19.99"));
       when(item2.getQuantity()).thenReturn(1);
       when(item2.getShipment()).thenReturn(null);
       Map<String, Object> snapshot = service.orderSnapshot(orderId);
       assertEquals(orderId, snapshot.get("orderId"));
       assertEquals("NEW", snapshot.get("orderStatus"));
       Object customerMapObj = snapshot.get("customer");
       assertTrue(customerMapObj instanceof Map);
       Map<?, ?> customerMap = (Map<?, ?>) customerMapObj;
       assertEquals(1001, customerMap.get("id"));
       assertEquals("Alice W", customerMap.get("fullName"));
       assertEquals("alice@example.com", customerMap.get("email"));
       Object storeMapObj = snapshot.get("store");
       assertTrue(storeMapObj instanceof Map);
       Map<?, ?> storeMap = (Map<?, ?>) storeMapObj;
       assertEquals(2001, storeMap.get("id"));
       assertEquals("Main Store", storeMap.get("storeName"));
       assertEquals("https://store.test", storeMap.get("webAddress"));
       Object productsObj = snapshot.get("products");
       assertTrue(productsObj instanceof List);
       List<?> products = (List<?>) productsObj;
       assertEquals(2, products.size());
       Map<?, ?> prod1 = (Map<?, ?>) products.get(0);
       assertEquals(301, prod1.get("productId"));
       assertEquals("Phone", prod1.get("productName"));
       assertEquals(new BigDecimal("499.99"), prod1.get("unitPrice"));
       assertEquals(2, prod1.get("quantity"));
       assertEquals("PENDING", prod1.get("shipmentStatus"));
       Map<?, ?> prod2 = (Map<?, ?>) products.get(1);
       assertEquals(302, prod2.get("productId"));
       assertEquals("Case", prod2.get("productName"));
       assertEquals(new BigDecimal("19.99"), prod2.get("unitPrice"));
       assertEquals(1, prod2.get("quantity"));
       assertNull(prod2.get("shipmentStatus"));
   }
   @Test
   @DisplayName("orderSnapshot() throws NotFound when order not found")
   void orderSnapshot_orderNotFound() {
       when(orderRepo.findById(999)).thenReturn(Optional.empty());
       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.orderSnapshot(999));
       assertEquals("Store, product, and customer data for the specified order ID not found.", ex.getMessage());
       verify(itemRepo, never()).findByOrderId(anyInt());
   }
   @Test
   @DisplayName("orderSnapshot() throws NotFound when order items empty")
   void orderSnapshot_itemsEmpty() {
       Integer orderId = 55;
       Order order = mock(Order.class);
       when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
       when(itemRepo.findByOrderId(orderId)).thenReturn(List.of());
       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.orderSnapshot(orderId));
       assertEquals("Store, product, and customer data for the specified order ID not found.", ex.getMessage());
   }
   // ---------------------------------------------------------
   // byCategoryRequired(String)
   // ---------------------------------------------------------
   @Test
   @DisplayName("byCategoryRequired() matches by brand")
   void byCategoryRequired_brandMatch() {
       Inventory invBrand = mock(Inventory.class);
       Inventory invOther = mock(Inventory.class);
       Product prodBrand = mock(Product.class);
       Product prodOther = mock(Product.class);
       when(invBrand.getProduct()).thenReturn(prodBrand);
       when(prodBrand.getBrand()).thenReturn("Nike");
       //when(prodBrand.getColour()).thenReturn("Red");
       when(invOther.getProduct()).thenReturn(prodOther);
       when(prodOther.getBrand()).thenReturn("Adidas");
       when(prodOther.getColour()).thenReturn("Blue");
       InventoryDTO dtoBrand = InventoryDTO.builder().inventoryId(10).productName("P1").build();
       when(repo.findAll()).thenReturn(List.of(invBrand, invOther));
       when(mapper.toDto(invBrand)).thenReturn(dtoBrand);
       List<InventoryDTO> out = service.byCategoryRequired("NIKE");
       assertEquals(1, out.size());
       assertEquals(10, out.get(0).getInventoryId());
   }
   @Test
   @DisplayName("byCategoryRequired() matches by colour with trimming and case-insensitive")
   void byCategoryRequired_colourMatchTrimmed() {
       Inventory inv = mock(Inventory.class);
       Product prod = mock(Product.class);
       when(inv.getProduct()).thenReturn(prod);
       when(prod.getBrand()).thenReturn(null);
       when(prod.getColour()).thenReturn("Black");
       InventoryDTO dto = InventoryDTO.builder().inventoryId(11).productName("P2").build();
       when(repo.findAll()).thenReturn(List.of(inv));
       when(mapper.toDto(inv)).thenReturn(dto);
       List<InventoryDTO> out = service.byCategoryRequired("  black  ");
       assertEquals(1, out.size());
       assertEquals(11, out.get(0).getInventoryId());
   }
   @Test
   @DisplayName("byCategoryRequired() throws NotFound when no matches")
   void byCategoryRequired_notFound() {
       Inventory inv = mock(Inventory.class);
       Product prod = mock(Product.class);
       when(inv.getProduct()).thenReturn(prod);
       when(prod.getBrand()).thenReturn("Puma");
       when(prod.getColour()).thenReturn("White");
       when(repo.findAll()).thenReturn(List.of(inv));
       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byCategoryRequired("Green"));
       assertEquals("Inventory records for the specified category not found.", ex.getMessage());
       verify(mapper, never()).toDto(any());
   }
}