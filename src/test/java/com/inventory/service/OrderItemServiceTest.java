package com.inventory.service;

import com.order.inventory.dto.OrderItemDTO;
import com.order.inventory.entity.Order;
import com.order.inventory.entity.OrderItem;
import com.order.inventory.entity.Product;
import com.order.inventory.entity.Shipment;
import com.order.inventory.entity.ShipmentStatus;
import com.order.inventory.mapper.OrderItemMapper;
import com.order.inventory.repository.OrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository repo;

    @Mock
    private OrderItemMapper mapper;

    @InjectMocks
    private OrderItemService service;

    private Order order;
    private OrderItem i1;
    private OrderItem i2;
    private OrderItemDTO d1;
    private OrderItemDTO d2;

    @BeforeEach
    void setUp() {
        // --- Minimal domain setup (fields only needed if your mapper uses them;
        // we mock the mapper below anyway)
        order = new Order();
        order.setId(1);

        Product p1 = new Product();
        p1.setId(100);
        p1.setProductName("Phone X");

        Product p2 = new Product();
        p2.setId(200);
        p2.setProductName("Laptop Z");

        Shipment shp = new Shipment();
        shp.setShipmentStatus(ShipmentStatus.PENDING);

        i1 = new OrderItem();
        // NOTE: In your schema, order_id is the PK on order_items; lineItemId is a column
        i1.setId(order.getId());          // order_id (PK)
        i1.setOrder(order);
        i1.setLineItemId(11);
        i1.setProduct(p1);
        i1.setUnitPrice(new BigDecimal("199.99"));
        i1.setQuantity(2);
        i1.setShipment(shp);

        i2 = new OrderItem();
        i2.setId(order.getId());          // same order_id (PK)
        i2.setOrder(order);
        i2.setLineItemId(12);
        i2.setProduct(p2);
        i2.setUnitPrice(new BigDecimal("999.00"));
        i2.setQuantity(1);
        i2.setShipment(null);

        d1 = OrderItemDTO.builder()
                .orderId(order.getId())
                .lineItemId(11)
                .productId(100)
                .productName("Phone X")
                .unitPrice(new BigDecimal("199.99"))
                .quantity(2)
                .shipmentStatus("PENDING")
                .build();

        d2 = OrderItemDTO.builder()
                .orderId(order.getId())
                .lineItemId(12)
                .productId(200)
                .productName("Laptop Z")
                .unitPrice(new BigDecimal("999.00"))
                .quantity(1)
                .shipmentStatus(null)
                .build();
    }

    // ---------------------------------------------------------------------
    // byOrder(orderId)
    // ---------------------------------------------------------------------

    @Test
    void byOrder_returnsMappedList_whenFound() {
        when(repo.findByOrderId(1)).thenReturn(List.of(i1, i2));
        when(mapper.toDto(i1)).thenReturn(d1);
        when(mapper.toDto(i2)).thenReturn(d2);

        List<OrderItemDTO> out = service.byOrder(1);

        assertThat(out).containsExactly(d1, d2);
        verify(repo).findByOrderId(1);
        verify(mapper).toDto(i1);
        verify(mapper).toDto(i2);
        verifyNoMoreInteractions(repo, mapper);
    }

    @Test
    void byOrder_returnsEmpty_whenNoItems() {
        when(repo.findByOrderId(999)).thenReturn(List.of());

        List<OrderItemDTO> out = service.byOrder(999);

        assertThat(out).isEmpty();
        verify(repo).findByOrderId(999);
        verifyNoInteractions(mapper);
    }
}