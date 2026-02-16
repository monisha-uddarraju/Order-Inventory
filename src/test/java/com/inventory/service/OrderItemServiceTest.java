package com.inventory.service;
import com.order.inventory.service.OrderItemService;
import com.order.inventory.dto.OrderItemDTO;

import com.order.inventory.entity.OrderItem;

import com.order.inventory.mapper.OrderItemMapper;

import com.order.inventory.repository.OrderItemRepository;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)

class OrderItemServiceTest {
   @Mock

   private OrderItemRepository repo;
   @Mock

   private OrderItemMapper mapper;
   @InjectMocks

   private OrderItemService service;
   @Test

   @DisplayName("byOrder() maps repository items to DTOs when present")

   void byOrder_mapsItems() {

       Integer orderId = 42;
       // mock domain items

       OrderItem i1 = mock(OrderItem.class);

       OrderItem i2 = mock(OrderItem.class);
       // expected mapped DTOs

       OrderItemDTO d1 = OrderItemDTO.builder()

               .orderId(orderId)

               .lineItemId(1)

               .productId(1001)

               .productName("Phone")

               .unitPrice(new BigDecimal("499.99"))

               .quantity(2)

               .shipmentStatus("PENDING")

               .build();
       OrderItemDTO d2 = OrderItemDTO.builder()

               .orderId(orderId)

               .lineItemId(2)

               .productId(1002)

               .productName("Case")

               .unitPrice(new BigDecimal("19.99"))

               .quantity(1)

               .shipmentStatus(null)

               .build();
       when(repo.findByOrderId(orderId)).thenReturn(List.of(i1, i2));

       when(mapper.toDto(i1)).thenReturn(d1);

       when(mapper.toDto(i2)).thenReturn(d2);
       List<OrderItemDTO> out = service.byOrder(orderId);
       assertEquals(2, out.size());

       assertEquals(42, out.get(0).getOrderId());

       assertEquals(1, out.get(0).getLineItemId());

       assertEquals(1001, out.get(0).getProductId());

       assertEquals("Phone", out.get(0).getProductName());

       assertEquals(new BigDecimal("499.99"), out.get(0).getUnitPrice());

       assertEquals(2, out.get(0).getQuantity());

       assertEquals("PENDING", out.get(0).getShipmentStatus());
       assertEquals(42, out.get(1).getOrderId());

       assertEquals(2, out.get(1).getLineItemId());

       assertEquals(1002, out.get(1).getProductId());

       assertEquals("Case", out.get(1).getProductName());

       assertEquals(new BigDecimal("19.99"), out.get(1).getUnitPrice());

       assertEquals(1, out.get(1).getQuantity());

       assertNull(out.get(1).getShipmentStatus());
       verify(repo, times(1)).findByOrderId(orderId);

       verify(mapper, times(1)).toDto(i1);

       verify(mapper, times(1)).toDto(i2);

   }
   @Test

   @DisplayName("byOrder() returns empty list when repository returns no items")

   void byOrder_emptyList() {

       Integer orderId = 999;
       when(repo.findByOrderId(orderId)).thenReturn(List.of());
       List<OrderItemDTO> out = service.byOrder(orderId);
       assertTrue(out.isEmpty());

       verify(repo, times(1)).findByOrderId(orderId);

       verify(mapper, never()).toDto(any());

   }

}
 