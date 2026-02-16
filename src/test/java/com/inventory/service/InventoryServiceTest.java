package com.inventory.service;

import com.order.inventory.dto.InventoryDTO;
import com.order.inventory.entity.*;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.InventoryMapper;
import com.order.inventory.repository.InventoryRepository;
import com.order.inventory.repository.OrderItemRepository;
import com.order.inventory.repository.OrderRepository;
import com.order.inventory.service.InventoryService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository repo;
    @Mock InventoryMapper mapper;
    @Mock OrderRepository orderRepo;
    @Mock OrderItemRepository itemRepo;

    @InjectMocks InventoryService service;

    Inventory inv;
    InventoryDTO dto;

    @BeforeEach
    void setup() {
        inv = mock(Inventory.class);
        dto = InventoryDTO.builder().inventoryId(1).storeId(10).productId(100).quantity(5).build();
    }

    // --------------------------- all() ---------------------------
    @Test
    void all_returnsMappedList() {
        Inventory inv2 = mock(Inventory.class);
        InventoryDTO dto2 = InventoryDTO.builder().inventoryId(2).storeId(20).productId(200).quantity(15).build();

        when(repo.findAll()).thenReturn(List.of(inv, inv2));
        when(mapper.toDto(inv)).thenReturn(dto);
        when(mapper.toDto(inv2)).thenReturn(dto2);

        var out = service.all();

        assertEquals(2, out.size());
    }

    // --------------------------- byStoreRequired ---------------------------
    @Test
    void byStoreRequired_found() {
        when(repo.findByStoreId(10)).thenReturn(List.of(inv));
        when(mapper.toDto(inv)).thenReturn(dto);

        var out = service.byStoreRequired(10);

        assertEquals(1, out.size());
    }

    @Test
    void byStoreRequired_notFound() {
        when(repo.findByStoreId(99)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> service.byStoreRequired(99));
    }

    // --------------------------- byProductAndStoreRequired ---------------------------
    @Test
    void byProductAndStoreRequired_found() {
        when(repo.findByProductAndStore(100, 10)).thenReturn(List.of(inv));
        when(mapper.toDto(inv)).thenReturn(dto);

        var out = service.byProductAndStoreRequired(100, 10);

        assertEquals(1, out.size());
    }

    @Test
    void byProductAndStoreRequired_empty() {
        when(repo.findByProductAndStore(1, 2)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> service.byProductAndStoreRequired(1, 2));
    }

    // --------------------------- inventoriesWithShipments ---------------------------
    @Test
    void inventoriesWithShipments_returnsMappedList() {
        Inventory inv2 = mock(Inventory.class);
        InventoryDTO dto2 = InventoryDTO.builder().inventoryId(2).build();

        when(repo.findInventoriesWithAnyShipmentForTheirProduct()).thenReturn(List.of(inv, inv2));
        when(mapper.toDto(inv)).thenReturn(dto);
        when(mapper.toDto(inv2)).thenReturn(dto2);

        var out = service.inventoriesWithShipments();

        assertEquals(2, out.size());
    }

    // --------------------------- orderSnapshot ---------------------------
    @Test
    void orderSnapshot_success() {
        int orderId = 77;

        // Order
        Order order = mock(Order.class);
        Customer cust = mock(Customer.class);
        Store store = mock(Store.class);
        OrderItem i1 = mock(OrderItem.class);
        OrderItem i2 = mock(OrderItem.class);

        when(orderRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(order.getCustomer()).thenReturn(cust);
        when(order.getStore()).thenReturn(store);
        when(order.getOrderStatus()).thenReturn(OrderStatus.NEW);

        when(cust.getId()).thenReturn(101);
        when(cust.getFullName()).thenReturn("Alice");
        when(cust.getEmailAddress()).thenReturn("a@x.com");

        when(store.getId()).thenReturn(501);
        when(store.getStoreName()).thenReturn("Main");
        when(store.getWebAddress()).thenReturn("https://s.com");

        // Items
        Product p1 = mock(Product.class);
        Product p2 = mock(Product.class);
        Shipment sh = mock(Shipment.class);

        when(itemRepo.findByOrderId(orderId)).thenReturn(List.of(i1, i2));

        when(i1.getProduct()).thenReturn(p1);
        when(p1.getId()).thenReturn(1);
        when(p1.getProductName()).thenReturn("Phone");
        when(i1.getUnitPrice()).thenReturn(new BigDecimal("100"));
        when(i1.getQuantity()).thenReturn(2);
        when(i1.getShipment()).thenReturn(sh);
        when(sh.getShipmentStatus()).thenReturn(ShipmentStatus.PENDING);

        when(i2.getProduct()).thenReturn(p2);
        when(p2.getId()).thenReturn(2);
        when(p2.getProductName()).thenReturn("Case");
        when(i2.getUnitPrice()).thenReturn(new BigDecimal("20"));
        when(i2.getQuantity()).thenReturn(1);
        when(i2.getShipment()).thenReturn(null);

        var snap = service.orderSnapshot(orderId);

        assertEquals(orderId, snap.get("orderId"));
        assertEquals("NEW", snap.get("orderStatus"));
        assertEquals(2, ((List<?>) snap.get("products")).size());
    }

    @Test
    void orderSnapshot_orderNotFound() {
        when(orderRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.orderSnapshot(999));
    }

    @Test
    void orderSnapshot_itemsEmpty() {
        when(orderRepo.findById(55)).thenReturn(Optional.of(mock(Order.class)));
        when(itemRepo.findByOrderId(55)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> service.orderSnapshot(55));
    }

    // --------------------------- byCategoryRequired ---------------------------
    @Test
    void byCategoryRequired_brandMatch() {
        Inventory inv1 = mock(Inventory.class);
        Product prod = mock(Product.class);

        when(inv1.getProduct()).thenReturn(prod);
        when(prod.getBrand()).thenReturn("Nike");
        InventoryDTO dtoBrand = InventoryDTO.builder().inventoryId(10).build();

        when(repo.findAll()).thenReturn(List.of(inv1));
        when(mapper.toDto(inv1)).thenReturn(dtoBrand);

        var out = service.byCategoryRequired("NIKE");

        assertEquals(1, out.size());
    }

    @Test
    void byCategoryRequired_colourMatch() {
        Inventory inv1 = mock(Inventory.class);
        Product prod = mock(Product.class);

        when(inv1.getProduct()).thenReturn(prod);
        when(prod.getBrand()).thenReturn(null);
        when(prod.getColour()).thenReturn("Black");

        InventoryDTO dtoColour = InventoryDTO.builder().inventoryId(11).build();

        when(repo.findAll()).thenReturn(List.of(inv1));
        when(mapper.toDto(inv1)).thenReturn(dtoColour);

        var out = service.byCategoryRequired("  black ");

        assertEquals(1, out.size());
    }

    @Test
    void byCategoryRequired_notFound() {
        Inventory inv1 = mock(Inventory.class);
        Product prod = mock(Product.class);

        when(inv1.getProduct()).thenReturn(prod);
        when(prod.getBrand()).thenReturn("Puma");
        when(prod.getColour()).thenReturn("White");

        when(repo.findAll()).thenReturn(List.of(inv1));

        assertThrows(NotFoundException.class,
                () -> service.byCategoryRequired("Green"));

        verify(mapper, never()).toDto(any());
    }
}