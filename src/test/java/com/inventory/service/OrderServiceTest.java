package com.inventory.service;

import com.order.inventory.dto.OrderDTO;
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
import com.order.inventory.service.OrderService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepo;
    @Mock OrderItemRepository itemRepo;
    @Mock CustomerRepository customerRepo;
    @Mock StoreRepository storeRepo;
    @Mock OrderMapper orderMapper;
    @Mock OrderItemMapper itemMapper;

    @InjectMocks OrderService service;

    // -------- all / get --------
    @Test
    void all_and_get() {
        Order o1 = new Order(); o1.setId(1);
        Order o2 = new Order(); o2.setId(2);
        when(orderRepo.findAll()).thenReturn(List.of(o1, o2));
        when(orderMapper.toDto(o1)).thenReturn(OrderDTO.builder().id(1).build());
        when(orderMapper.toDto(o2)).thenReturn(OrderDTO.builder().id(2).build());
        assertEquals(2, service.all().size());

        when(orderRepo.findById(10)).thenReturn(Optional.of(o1));
        when(orderMapper.toDto(o1)).thenReturn(OrderDTO.builder().id(1).build());
        assertEquals(1, service.get(10).getId());

        when(orderRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(99));
    }

    // -------- create --------
    @Test
    void create_happy_and_errors() {
        var tms = Instant.parse("2024-01-02T10:00:00Z");
        var in = OrderDTO.builder().customerId(1).storeId(2).status("new").orderTms(tms).build();

        Customer c = new Customer(); c.setId(1);
        Store s = new Store(); s.setId(2);
        Order saved = new Order(); saved.setId(77);

        when(customerRepo.findById(1)).thenReturn(Optional.of(c));
        when(storeRepo.findById(2)).thenReturn(Optional.of(s));
        when(orderRepo.save(any())).thenReturn(saved);
        when(orderMapper.toDto(saved)).thenReturn(OrderDTO.builder().id(77).build());

        assertEquals(77, service.create(in).getId());

        assertThrows(BadRequestException.class, () -> service.create(OrderDTO.builder().build()));

        when(customerRepo.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                service.create(OrderDTO.builder().customerId(1).storeId(2).status("NEW").build()));

        when(customerRepo.findById(1)).thenReturn(Optional.of(c));
        when(storeRepo.findById(2)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () ->
                service.create(OrderDTO.builder().customerId(1).storeId(2).status("NEW").build()));

        when(storeRepo.findById(2)).thenReturn(Optional.of(s));
        assertThrows(BadRequestException.class, () ->
                service.create(OrderDTO.builder().customerId(1).storeId(2).status("BAD").build()));
    }

    // -------- update --------
    @Test
    void update_happy_invalid_and_404() {
        Order o = new Order(); o.setId(50);
        Customer c2 = new Customer(); c2.setId(3);
        Store s2 = new Store(); s2.setId(4);
        var patch = OrderDTO.builder().status("complete")
                .orderTms(Instant.parse("2024-02-01T12:00:00Z")).customerId(3).storeId(4).build();

        when(orderRepo.findById(50)).thenReturn(Optional.of(o));
        when(customerRepo.findById(3)).thenReturn(Optional.of(c2));
        when(storeRepo.findById(4)).thenReturn(Optional.of(s2));
        when(orderRepo.save(o)).thenReturn(o);
        when(orderMapper.toDto(o)).thenReturn(OrderDTO.builder().id(50).build());
        assertEquals(50, service.update(50, patch).getId());

        when(orderRepo.findById(9)).thenReturn(Optional.of(new Order()));
        assertThrows(BadRequestException.class, () ->
                service.update(9, OrderDTO.builder().status("bad").build()));

        when(orderRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.update(99, new OrderDTO()));
    }

    // -------- delete --------
    @Test
    void delete_happy_and_404() {
        when(orderRepo.existsById(5)).thenReturn(true);
        service.delete(5);
        verify(orderRepo).deleteById(5);

        when(orderRepo.existsById(77)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.delete(77));
    }

    // -------- aggregations / filters --------
    @Test
    void count_and_filters() {
        when(orderRepo.countOrdersByStatus()).thenReturn(List.of(
                new Object[]{OrderStatus.NEW, 3L},
                new Object[]{OrderStatus.COMPLETE, 5L}
        ));
        var counts = service.countByStatus();
        assertEquals(3L, counts.get("NEW"));
        assertEquals(5L, counts.get("COMPLETE"));

        Order o = new Order(); o.setId(1);
        when(orderRepo.findByOrderStatus(OrderStatus.NEW)).thenReturn(List.of(o));
        when(orderMapper.toDto(o)).thenReturn(OrderDTO.builder().id(1).build());
        assertEquals(1, service.byStatus("new").size());
        assertThrows(BadRequestException.class, () -> service.byStatus("INVALID"));

        when(orderRepo.findByCustomerId(11)).thenReturn(List.of(o));
        assertEquals(1, service.byCustomer(11).size());

        when(orderRepo.findByCustomerId(123)).thenReturn(List.of());
        assertThrows(NotFoundException.class, () -> service.byCustomerRequired(123));

        when(orderRepo.findByCustomerEmail("a@b.com")).thenReturn(List.of(o));
        assertEquals(1, service.byCustomerEmail("a@b.com").size());

        when(orderRepo.findByStoreName("Main")).thenReturn(List.of(o));
        assertEquals(1, service.byStoreName("Main").size());
    }
}
