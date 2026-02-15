package com.inventory.service;

import com.order.inventory.dto.InventoryDTO;
import com.order.inventory.entity.Customer;
import com.order.inventory.entity.Inventory;
import com.order.inventory.entity.Order;
import com.order.inventory.entity.OrderItem;
import com.order.inventory.entity.OrderStatus;
import com.order.inventory.entity.Product;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.entity.Store;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.InventoryMapper;
import com.order.inventory.repository.InventoryRepository;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.OrderRepository;
import com.order.inventory.service.InventoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

    // ---- Shared fixtures
    private Store store1;
    private Store store2;
    private Product prodPhoneRed;
    private Product prodLaptopBlue;
    private Inventory inv1;
    private Inventory inv2;
    private InventoryDTO dto1;
    private InventoryDTO dto2;

    @BeforeEach
    void setUp() {
        // Stores
        store1 = new Store();
        store1.setId(1);
        store1.setStoreName("Main Store");
        store1.setWebAddress("https://main.example.com");

        store2 = new Store();
        store2.setId(2);
        store2.setStoreName("Outlet");
        store2.setWebAddress("https://outlet.example.com");

        // Products
        prodPhoneRed = new Product();
        prodPhoneRed.setId(100);
        prodPhoneRed.setProductName("Phone X");
        prodPhoneRed.setBrand("ACME");
        prodPhoneRed.setColour("Red");

        prodLaptopBlue = new Product();
        prodLaptopBlue.setId(200);
        prodLaptopBlue.setProductName("Laptop Z");
        prodLaptopBlue.setBrand("ZETA");
        prodLaptopBlue.setColour("Blue");

        // Inventories
        inv1 = Inventory.builder()
                .id(10)
                .store(store1)
                .product(prodPhoneRed)
                .productInventory(25)
                .build();

        inv2 = Inventory.builder()
                .id(11)
                .store(store2)
                .product(prodLaptopBlue)
                .productInventory(5)
                .build();

        // DTOs expected from mapper
        dto1 = InventoryDTO.builder()
                .inventoryId(10)
                .storeId(store1.getId())
                .storeName(store1.getStoreName())
                .productId(prodPhoneRed.getId())
                .productName(prodPhoneRed.getProductName())
                .quantity(25)
                .build();

        dto2 = InventoryDTO.builder()
                .inventoryId(11)
                .storeId(store2.getId())
                .storeName(store2.getStoreName())
                .productId(prodLaptopBlue.getId())
                .productName(prodLaptopBlue.getProductName())
                .quantity(5)
                .build();
    }

    // ---------------------------------------------------------
    // all()
    // ---------------------------------------------------------
    @Test
    void all_returnsMappedList() {
        when(repo.findAll()).thenReturn(List.of(inv1, inv2));
        when(mapper.toDto(inv1)).thenReturn(dto1);
        when(mapper.toDto(inv2)).thenReturn(dto2);

        List<InventoryDTO> out = service.all();

        assertThat(out).containsExactly(dto1, dto2);
        verify(repo).findAll();
        verify(mapper).toDto(inv1);
        verify(mapper).toDto(inv2);
        verifyNoMoreInteractions(repo, mapper);
    }

    // ---------------------------------------------------------
    // byStoreRequired(storeId)
    // ---------------------------------------------------------
    @Test
    void byStoreRequired_returnsList_whenFound() {
        when(repo.findByStoreId(1)).thenReturn(List.of(inv1));
        when(mapper.toDto(inv1)).thenReturn(dto1);

        List<InventoryDTO> out = service.byStoreRequired(1);

        assertThat(out).containsExactly(dto1);
        verify(repo).findByStoreId(1);
        verify(mapper).toDto(inv1);
    }

    @Test
    void byStoreRequired_throws_whenEmpty() {
        when(repo.findByStoreId(99)).thenReturn(List.of());

        assertThatThrownBy(() -> service.byStoreRequired(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("store ID not found");

        verify(repo).findByStoreId(99);
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------
    // byProductAndStoreRequired(productId, storeId)
    // ---------------------------------------------------------
    @Test
    void byProductAndStoreRequired_returnsList_whenFound() {
        when(repo.findByProductAndStore(100, 1)).thenReturn(List.of(inv1));
        when(mapper.toDto(inv1)).thenReturn(dto1);

        List<InventoryDTO> out = service.byProductAndStoreRequired(100, 1);

        assertThat(out).containsExactly(dto1);
        verify(repo).findByProductAndStore(100, 1);
        verify(mapper).toDto(inv1);
    }

    @Test
    void byProductAndStoreRequired_throws_whenEmpty() {
        when(repo.findByProductAndStore(999, 888)).thenReturn(List.of());

        assertThatThrownBy(() -> service.byProductAndStoreRequired(999, 888))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("product and store not found");

        verify(repo).findByProductAndStore(999, 888);
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------
    // inventoriesWithShipments()
    // ---------------------------------------------------------
    @Test
    void inventoriesWithShipments_returnsMappedList() {
        when(repo.findInventoriesWithAnyShipmentForTheirProduct()).thenReturn(List.of(inv2));
        when(mapper.toDto(inv2)).thenReturn(dto2);

        List<InventoryDTO> out = service.inventoriesWithShipments();

        assertThat(out).containsExactly(dto2);
        verify(repo).findInventoriesWithAnyShipmentForTheirProduct();
        verify(mapper).toDto(inv2);
    }

    // ---------------------------------------------------------
    // orderSnapshot(orderId)
    // ---------------------------------------------------------
    @Nested
    class OrderSnapshotTests {

        private Order order;
        private Customer customer;
        private Shipment shipmentPending;
        private OrderItem item1;
        private OrderItem item2;

        @BeforeEach
        void snapshotSetup() {
            customer = Customer.builder()
                    .id(5000)
                    .fullName("Veda Sri")
                    .emailAddress("veda@example.com")
                    .build();

            order = new Order();
            order.setId(123);
            order.setOrderStatus(OrderStatus.NEW);
            order.setCustomer(customer);
            order.setStore(store1);

            shipmentPending = new Shipment();
            shipmentPending.setShipmentStatus(ShipmentStatus.PENDING);

            item1 = new OrderItem();
            item1.setOrder(order);
            item1.setProduct(prodPhoneRed);
            item1.setQuantity(2);
            item1.setUnitPrice(new BigDecimal("199.99"));
            item1.setShipment(shipmentPending);

            item2 = new OrderItem();
            item2.setOrder(order);
            item2.setProduct(prodLaptopBlue);
            item2.setQuantity(1);
            item2.setUnitPrice(new BigDecimal("999.00"));
            item2.setShipment(null);
        }

        @Test
        void orderSnapshot_returnsComposedMap_whenFound() {
            when(orderRepo.findById(123)).thenReturn(Optional.of(order));
            when(itemRepo.findByOrderId(123)).thenReturn(List.of(item1, item2));

            Map<String, Object> snapshot = service.orderSnapshot(123);

            assertThat(snapshot)
                    .containsEntry("orderId", 123)
                    .containsEntry("orderStatus", "NEW")
                    .containsKeys("customer", "store", "products");

            // Customer
            Map<String, Object> cust = cast(snapshot.get("customer"));
            assertThat(cust).containsEntry("id", 5000)
                            .containsEntry("fullName", "Veda Sri")
                            .containsEntry("email", "veda@example.com");

            // Store
            Map<String, Object> sto = cast(snapshot.get("store"));
            assertThat(sto).containsEntry("id", store1.getId())
                           .containsEntry("storeName", store1.getStoreName())
                           .containsEntry("webAddress", store1.getWebAddress());

            // Products
            List<Map<String, Object>> products = cast(snapshot.get("products"));
            assertThat(products).hasSize(2);

            assertThat(products.get(0))
                    .containsEntry("productId", prodPhoneRed.getId())
                    .containsEntry("productName", prodPhoneRed.getProductName())
                    .containsEntry("quantity", 2)
                    .containsEntry("shipmentStatus", "PENDING")
                    .containsKey("unitPrice");

            assertThat(products.get(1))
                    .containsEntry("productId", prodLaptopBlue.getId())
                    .containsEntry("productName", prodLaptopBlue.getProductName())
                    .containsEntry("quantity", 1)
                    .containsEntry("shipmentStatus", null)
                    .containsKey("unitPrice");

            verify(orderRepo).findById(123);
            verify(itemRepo).findByOrderId(123);
        }

        @Test
        void orderSnapshot_throws_whenOrderMissing() {
            when(orderRepo.findById(404)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.orderSnapshot(404))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("order ID not found");

            verify(orderRepo).findById(404);
            verifyNoInteractions(itemRepo);
        }

        @Test
        void orderSnapshot_throws_whenItemsEmpty() {
            when(orderRepo.findById(123)).thenReturn(Optional.of(order));
            when(itemRepo.findByOrderId(123)).thenReturn(List.of());

            assertThatThrownBy(() -> service.orderSnapshot(123))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("order ID not found");

            verify(orderRepo).findById(123);
            verify(itemRepo).findByOrderId(123);
        }

        @SuppressWarnings("unchecked")
        private <T> T cast(Object o) {
            return (T) o;
        }
    }

    // ---------------------------------------------------------
    // byCategoryRequired(category)
    // ---------------------------------------------------------
    @Test
    void byCategoryRequired_returnsByBrand_caseInsensitive_trimmed() {
        when(repo.findAll()).thenReturn(List.of(inv1, inv2));
        when(mapper.toDto(inv1)).thenReturn(dto1); // inv1 brand = "ACME"

        List<InventoryDTO> out = service.byCategoryRequired("  acme ");

        assertThat(out).containsExactly(dto1);
        verify(repo).findAll();
        verify(mapper).toDto(inv1);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void byCategoryRequired_returnsByColour_caseInsensitive() {
        when(repo.findAll()).thenReturn(List.of(inv1, inv2));
        when(mapper.toDto(inv2)).thenReturn(dto2); // inv2 colour = "Blue"

        List<InventoryDTO> out = service.byCategoryRequired("blue");

        assertThat(out).containsExactly(dto2);
        verify(repo).findAll();
        verify(mapper).toDto(inv2);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void byCategoryRequired_throws_whenNoMatch() {
        when(repo.findAll()).thenReturn(List.of(inv1, inv2));

        assertThatThrownBy(() -> service.byCategoryRequired("green"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("category not found");

        verify(repo).findAll();
        verifyNoInteractions(mapper);
    }
}
