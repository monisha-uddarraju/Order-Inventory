package com.inventory.service;
import com.order.inventory.dto.OrderDTO;
import com.order.inventory.service.OrderService;
import com.order.inventory.dto.OrderDTO.Details;

import com.order.inventory.dto.OrderDTO.LineItem;

import com.order.inventory.entity.Customer;

import com.order.inventory.entity.Order;

import com.order.inventory.entity.OrderStatus;

import com.order.inventory.entity.Store;

import com.order.inventory.exception.BadRequestException;

import com.order.inventory.exception.NotFoundException;

import com.order.inventory.mapper.OrderItemMapper;

import com.order.inventory.mapper.OrderMapper;

import com.order.inventory.repository.CustomerRepository;

import com.order.inventory.repository.OrderItemRepository;

import com.order.inventory.repository.OrderRepository;

import com.order.inventory.repository.StoreRepository;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Nested;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;

import java.time.Instant;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)

class OrderServiceTest {
   @Mock private OrderRepository orderRepo;

   @Mock private OrderItemRepository itemRepo;

   @Mock private CustomerRepository customerRepo;

   @Mock private StoreRepository storeRepo;

   @Mock private OrderMapper orderMapper;

   @Mock private OrderItemMapper itemMapper;
   @InjectMocks

   private OrderService service;
   // ---------------------------------------------------------------------

   // Basic CRUD / Reads

   // ---------------------------------------------------------------------
   @Test

   @DisplayName("all() returns mapped list")

   void all_returnsMappedList() {

       Order o1 = new Order(); o1.setId(1);

       Order o2 = new Order(); o2.setId(2);
       OrderDTO d1 = OrderDTO.builder().id(1).build();

       OrderDTO d2 = OrderDTO.builder().id(2).build();
       when(orderRepo.findAll()).thenReturn(List.of(o1, o2));

       when(orderMapper.toDto(o1)).thenReturn(d1);

       when(orderMapper.toDto(o2)).thenReturn(d2);
       List<OrderDTO> out = service.all();
       assertEquals(2, out.size());

       assertEquals(1, out.get(0).getId());

       assertEquals(2, out.get(1).getId());

   }
   @Test

   @DisplayName("get() returns DTO when found")

   void get_found() {

       Order o = new Order(); o.setId(10);

       OrderDTO dto = OrderDTO.builder().id(10).build();
       when(orderRepo.findById(10)).thenReturn(Optional.of(o));

       when(orderMapper.toDto(o)).thenReturn(dto);
       OrderDTO out = service.get(10);
       assertEquals(10, out.getId());

   }
   @Test

   @DisplayName("get() throws NotFound when missing")

   void get_notFound() {

       when(orderRepo.findById(99)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.get(99));

       assertEquals("Order not found", ex.getMessage());

   }
   @Test

   @DisplayName("create() succeeds with valid input")

   void create_success() {

       OrderDTO input = OrderDTO.builder()

               .customerId(1)

               .storeId(2)

               .status("new")

               .orderTms(Instant.parse("2024-01-02T10:00:00Z"))

               .build();
       Customer c = new Customer(); c.setId(1);

       Store s = new Store(); s.setId(2);
       Order saved = new Order(); saved.setId(77);

       OrderDTO savedDto = OrderDTO.builder().id(77).build();
       when(customerRepo.findById(1)).thenReturn(Optional.of(c));

       when(storeRepo.findById(2)).thenReturn(Optional.of(s));

       when(orderRepo.save(any(Order.class))).thenReturn(saved);

       when(orderMapper.toDto(saved)).thenReturn(savedDto);
       OrderDTO out = service.create(input);
       assertEquals(77, out.getId());
       ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

       verify(orderRepo).save(captor.capture());

       Order toSave = captor.getValue();

       assertEquals(c, toSave.getCustomer());

       assertEquals(s, toSave.getStore());

       assertEquals(OrderStatus.NEW, toSave.getOrderStatus());

       assertEquals(Instant.parse("2024-01-02T10:00:00Z"), toSave.getOrderTms());

   }
   @Test

   @DisplayName("create() rejects missing fields")

   void create_missingFields() {

       OrderDTO input = OrderDTO.builder().build();

       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(input));

       assertEquals("customerId, storeId and status are required", ex.getMessage());

   }
   @Test

   @DisplayName("create() throws NotFound when customer missing")

   void create_customerNotFound() {

       OrderDTO input = OrderDTO.builder().customerId(1).storeId(2).status("NEW").build();

       when(customerRepo.findById(1)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(input));

       assertEquals("Customer not found", ex.getMessage());

       verify(storeRepo, never()).findById(anyInt());

   }
   @Test

   @DisplayName("create() throws NotFound when store missing")

   void create_storeNotFound() {

       OrderDTO input = OrderDTO.builder().customerId(1).storeId(2).status("NEW").build();

       Customer c = new Customer(); c.setId(1);

       when(customerRepo.findById(1)).thenReturn(Optional.of(c));

       when(storeRepo.findById(2)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.create(input));

       assertEquals("Store not found", ex.getMessage());

   }
   @Test

   @DisplayName("create() rejects invalid status")

   void create_invalidStatus() {

       OrderDTO input = OrderDTO.builder().customerId(1).storeId(2).status("BAD").build();

       Customer c = new Customer(); c.setId(1);

       Store s = new Store(); s.setId(2);

       when(customerRepo.findById(1)).thenReturn(Optional.of(c));

       when(storeRepo.findById(2)).thenReturn(Optional.of(s));
       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(input));

       assertEquals("Invalid order status", ex.getMessage());

       verify(orderRepo, never()).save(any());

   }
   @Test

   @DisplayName("update() updates status, time, and relations when provided")

   void update_success_full() {

       // existing order

       Customer c1 = new Customer(); c1.setId(1);

       Store s1 = new Store(); s1.setId(2);

       Order o = new Order();

       o.setId(50);

       o.setCustomer(c1);

       o.setStore(s1);

       o.setOrderStatus(OrderStatus.NEW);

       o.setOrderTms(Instant.parse("2024-01-02T10:00:00Z"));
       // new relations

       Customer c2 = new Customer(); c2.setId(3);

       Store s2 = new Store(); s2.setId(4);
       OrderDTO patch = OrderDTO.builder()

               .status("complete")

               .orderTms(Instant.parse("2024-02-01T12:00:00Z"))

               .customerId(3)

               .storeId(4)

               .build();
       Order saved = new Order(); saved.setId(50);

       OrderDTO savedDto = OrderDTO.builder().id(50).build();
       when(orderRepo.findById(50)).thenReturn(Optional.of(o));

       when(customerRepo.findById(3)).thenReturn(Optional.of(c2));

       when(storeRepo.findById(4)).thenReturn(Optional.of(s2));

       when(orderRepo.save(o)).thenReturn(saved);

       when(orderMapper.toDto(saved)).thenReturn(savedDto);
       OrderDTO out = service.update(50, patch);
       assertEquals(50, out.getId());

       assertEquals(OrderStatus.COMPLETE, o.getOrderStatus());

       assertEquals(Instant.parse("2024-02-01T12:00:00Z"), o.getOrderTms());

       assertEquals(c2, o.getCustomer());

       assertEquals(s2, o.getStore());

   }
   @Test

   @DisplayName("update() rejects invalid status when provided")

   void update_invalidStatus() {

       Order o = new Order(); o.setId(9); o.setOrderStatus(OrderStatus.NEW);

       when(orderRepo.findById(9)).thenReturn(Optional.of(o));
       OrderDTO patch = OrderDTO.builder().status("bad").build();
       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.update(9, patch));

       assertEquals("Invalid order status", ex.getMessage());

       verify(orderRepo, never()).save(any());

   }
   @Test

   @DisplayName("update() throws NotFound when order missing")

   void update_notFound() {

       when(orderRepo.findById(99)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(99, OrderDTO.builder().build()));

       assertEquals("Order not found", ex.getMessage());

   }
   @Test

   @DisplayName("delete() deletes when exists")

   void delete_success() {

       when(orderRepo.existsById(5)).thenReturn(true);

       service.delete(5);

       verify(orderRepo).deleteById(5);

   }
   @Test

   @DisplayName("delete() throws NotFound when not exists")

   void delete_notFound() {

       when(orderRepo.existsById(77)).thenReturn(false);

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.delete(77));

       assertEquals("Order not found", ex.getMessage());

       verify(orderRepo, never()).deleteById(anyInt());

   }
   // ---------------------------------------------------------------------

   // Aggregations / Filters

   // ---------------------------------------------------------------------
   @Test

   @DisplayName("countByStatus() maps repository rows to status->count")

   void countByStatus_maps() {

       List<Object[]> rows = List.of(

               new Object[]{OrderStatus.NEW, 3L},

               new Object[]{OrderStatus.COMPLETE, 5}

       );

       when(orderRepo.countOrdersByStatus()).thenReturn(rows);
       Map<String, Long> out = service.countByStatus();
       assertEquals(2, out.size());

       assertEquals(3L, out.get("NEW"));

       assertEquals(5L, out.get("COMPLETE"));

   }
   @Test

   @DisplayName("byStatus() returns mapped list when valid status")

   void byStatus_valid() {

       Order o = new Order(); o.setId(1);

       OrderDTO d = OrderDTO.builder().id(1).build();
       when(orderRepo.findByOrderStatus(OrderStatus.NEW)).thenReturn(List.of(o));

       when(orderMapper.toDto(o)).thenReturn(d);
       List<OrderDTO> out = service.byStatus("new");
       assertEquals(1, out.size());

       assertEquals(1, out.get(0).getId());

   }
   @Test

   @DisplayName("byStatus() rejects invalid status")

   void byStatus_invalid() {

       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.byStatus("INVALID"));

       assertEquals("Invalid status", ex.getMessage());

   }
   @Test

   @DisplayName("byCustomer() returns mapped orders")

   void byCustomer_returnsMapped() {

       Order o = new Order(); o.setId(7);

       OrderDTO d = OrderDTO.builder().id(7).build();
       when(orderRepo.findByCustomerId(11)).thenReturn(List.of(o));

       when(orderMapper.toDto(o)).thenReturn(d);
       List<OrderDTO> out = service.byCustomer(11);
       assertEquals(1, out.size());

       assertEquals(7, out.get(0).getId());

   }
   @Test

   @DisplayName("byCustomerRequired() throws NotFound when empty")

   void byCustomerRequired_empty() {

       when(orderRepo.findByCustomerId(123)).thenReturn(List.of());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.byCustomerRequired(123));

       assertEquals("Orders for the specified customer ID not found.", ex.getMessage());

   }
   @Test

   @DisplayName("byCustomerEmail() returns mapped orders")

   void byCustomerEmail_returnsMapped() {

       Order o = new Order(); o.setId(8);

       OrderDTO d = OrderDTO.builder().id(8).build();
       when(orderRepo.findByCustomerEmail("a@b.com")).thenReturn(List.of(o));

       when(orderMapper.toDto(o)).thenReturn(d);
       List<OrderDTO> out = service.byCustomerEmail("a@b.com");
       assertEquals(1, out.size());

       assertEquals(8, out.get(0).getId());

   }
   @Test

   @DisplayName("byStoreName() returns mapped orders")

   void byStoreName_returnsMapped() {

       Order o = new Order(); o.setId(9);

       OrderDTO d = OrderDTO.builder().id(9).build();
       when(orderRepo.findByStoreName("Main")).thenReturn(List.of(o));

       when(orderMapper.toDto(o)).thenReturn(d);
       List<OrderDTO> out = service.byStoreName("Main");
       assertEquals(1, out.size());

       assertEquals(9, out.get(0).getId());

   }
   @Test

   @DisplayName("byDateRange() parses dates and maps results")

   void byDateRange_success() {

       Order o = new Order(); o.setId(12);

       OrderDTO d = OrderDTO.builder().id(12).build();
       when(orderRepo.findByDateRange(any(), any())).thenReturn(List.of(o));

       when(orderMapper.toDto(o)).thenReturn(d);
       List<OrderDTO> out = service.byDateRange("2024-01-01", "2024-01-31");
       assertEquals(1, out.size());

       assertEquals(12, out.get(0).getId());

   }
   @Test

   @DisplayName("byDateRange() rejects invalid date format")

   void byDateRange_invalidDate() {

       BadRequestException ex = assertThrows(BadRequestException.class,

               () -> service.byDateRange("2024-13-01", "2024-01-31"));

       assertEquals("Dates must be yyyy-MM-dd", ex.getMessage());

   }
   @Test

   @DisplayName("byDateRange() rejects start after end")

   void byDateRange_startAfterEnd() {

       BadRequestException ex = assertThrows(BadRequestException.class,

               () -> service.byDateRange("2024-02-10", "2024-02-01"));

       assertEquals("startDate must be <= endDate", ex.getMessage());

   }
   // ---------------------------------------------------------------------

   // Details / Cancel

   // ---------------------------------------------------------------------
   @Test

   @DisplayName("details() builds Details with storeName, first non-null shipmentStatus, items and total")

   void details_success() {

       // order with store

       Store store = new Store();

       store.setId(5);

       store.setStoreName("Main Store");

       Order o = new Order();

       o.setId(42);

       o.setStore(store);
       // items returned from repo (use proper mocks of OrderItem)

       com.order.inventory.entity.OrderItem orderItem1 = mock(com.order.inventory.entity.OrderItem.class);

       com.order.inventory.entity.OrderItem orderItem2 = mock(com.order.inventory.entity.OrderItem.class);
       LineItem li1 = LineItem.builder()

               .lineItemId(1)

               .shipmentStatus(null)

               .build();

       LineItem li2 = LineItem.builder()

               .lineItemId(2)

               .shipmentStatus("DELIVERED")

               .build();
       when(orderRepo.findById(42)).thenReturn(Optional.of(o));

       when(itemRepo.findByOrderId(42)).thenReturn(List.of(orderItem1, orderItem2));

       when(itemMapper.toLineItem(orderItem1)).thenReturn(li1);

       when(itemMapper.toLineItem(orderItem2)).thenReturn(li2);

       when(itemRepo.totalAmountByOrderId(42)).thenReturn(new java.math.BigDecimal("519.98"));
       OrderDTO.Details details = service.details(42);
       assertEquals(42, details.getOrderId());

       assertEquals("Main Store", details.getStoreName());

       assertEquals("DELIVERED", details.getShipmentStatus());

       assertEquals(new java.math.BigDecimal("519.98"), details.getTotalAmount());

       assertEquals(2, details.getItems().size());

       assertEquals(1, details.getItems().get(0).getLineItemId());

       assertEquals(2, details.getItems().get(1).getLineItemId());

   }
   @Test

   @DisplayName("details() throws NotFound when order missing")

   void details_notFound() {

       when(orderRepo.findById(1000)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.details(1000));

       assertEquals("Order not found", ex.getMessage());

   }
   @Test

   @DisplayName("cancel() sets status to CANCELLED and returns mapped DTO")

   void cancel_success() {

       Order o = new Order(); o.setId(55); o.setOrderStatus(OrderStatus.NEW);

       Order saved = new Order(); saved.setId(55); saved.setOrderStatus(OrderStatus.CANCELLED);

       OrderDTO dto = OrderDTO.builder().id(55).status("CANCELLED").build();
       when(orderRepo.findById(55)).thenReturn(Optional.of(o));

       when(orderRepo.save(o)).thenReturn(saved);

       when(orderMapper.toDto(saved)).thenReturn(dto);
       OrderDTO out = service.cancel(55);
       assertEquals(55, out.getId());

       assertEquals("CANCELLED", out.getStatus());

       assertEquals(OrderStatus.CANCELLED, o.getOrderStatus());

       verify(orderRepo).save(o);

   }
   @Test

   @DisplayName("cancel() throws NotFound when order missing")

   void cancel_notFound() {

       when(orderRepo.findById(404)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.cancel(404));

       assertEquals("Order not found", ex.getMessage());

   }
   // ---------------------------------------------------------------------

   // Additional focused tests for optional branch coverage

   // ---------------------------------------------------------------------
   @Nested

   @DisplayName("update() partial fields")

   class UpdatePartial {
       @Test

       @DisplayName("updates only orderTms when only time provided")

       void update_onlyTime() {

           Order o = new Order(); o.setId(70); o.setOrderTms(Instant.parse("2024-01-01T00:00:00Z"));

           when(orderRepo.findById(70)).thenReturn(Optional.of(o));
           Order saved = new Order(); saved.setId(70);

           OrderDTO savedDto = OrderDTO.builder().id(70).build();

           when(orderRepo.save(o)).thenReturn(saved);

           when(orderMapper.toDto(saved)).thenReturn(savedDto);
           OrderDTO patch = OrderDTO.builder()

                   .orderTms(Instant.parse("2024-03-01T00:00:00Z"))

                   .build();
           OrderDTO out = service.update(70, patch);
           assertEquals(70, out.getId());

           assertEquals(Instant.parse("2024-03-01T00:00:00Z"), o.getOrderTms());

       }
       @Test

       @DisplayName("reassigns only customer/store when provided")

       void update_onlyRelations() {

           Customer c1 = new Customer(); c1.setId(1);

           Store s1 = new Store(); s1.setId(2);

           Order o = new Order(); o.setId(71); o.setCustomer(c1); o.setStore(s1);
           when(orderRepo.findById(71)).thenReturn(Optional.of(o));
           Customer c2 = new Customer(); c2.setId(3);

           Store s2 = new Store(); s2.setId(4);

           when(customerRepo.findById(3)).thenReturn(Optional.of(c2));

           when(storeRepo.findById(4)).thenReturn(Optional.of(s2));
           Order saved = new Order(); saved.setId(71);

           OrderDTO savedDto = OrderDTO.builder().id(71).build();

           when(orderRepo.save(o)).thenReturn(saved);

           when(orderMapper.toDto(saved)).thenReturn(savedDto);
           OrderDTO patch = OrderDTO.builder().customerId(3).storeId(4).build();
           OrderDTO out = service.update(71, patch);
           assertEquals(71, out.getId());

           assertEquals(c2, o.getCustomer());

           assertEquals(s2, o.getStore());

       }

   }

}

 