package com.inventory.service;

import com.order.inventory.dto.OrderDTO;

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
import com.order.inventory.service.OrderService;

import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.*;
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

    // ---- shared fixtures
    private Customer cust1;
    private Store store1;
    private Order order1;
    private Order order2;
    private OrderDTO dto1;
    private OrderDTO dto2;

    @BeforeEach
    void setUp() {
        cust1 = Customer.builder().id(100).fullName("Veda Sri").emailAddress("veda@example.com").build();

        store1 = new Store();
        store1.setId(10);
        store1.setStoreName("Main Store");
        store1.setWebAddress("https://main.example.com");

        order1 = new Order();
        order1.setId(1);
        order1.setOrderStatus(OrderStatus.NEW);
        order1.setOrderTms(Instant.parse("2025-01-01T00:00:00Z"));
        order1.setCustomer(cust1);
        order1.setStore(store1);

        order2 = new Order();
        order2.setId(2);
        order2.setOrderStatus(OrderStatus.COMPLETE);
        order2.setOrderTms(Instant.parse("2025-01-02T00:00:00Z"));
        order2.setCustomer(cust1);
        order2.setStore(store1);

        dto1 = OrderDTO.builder()
                .id(1).status("NEW").orderTms(order1.getOrderTms())
                .customerId(cust1.getId()).storeId(store1.getId())
                .storeName(store1.getStoreName()).webAddress(store1.getWebAddress())
                .build();

        dto2 = OrderDTO.builder()
                .id(2).status("COMPLETE").orderTms(order2.getOrderTms())
                .customerId(cust1.getId()).storeId(store1.getId())
                .storeName(store1.getStoreName()).webAddress(store1.getWebAddress())
                .build();
    }

    // ---------------------------------------------------------------------
    // all()
    // ---------------------------------------------------------------------
    @Test
    void all_returnsMappedList() {
        when(orderRepo.findAll()).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        List<OrderDTO> out = service.all();

        assertThat(out).containsExactly(dto1, dto2);
        verify(orderRepo).findAll();
        verify(orderMapper).toDto(order1);
        verify(orderMapper).toDto(order2);
    }

    // ---------------------------------------------------------------------
    // get(id)
    // ---------------------------------------------------------------------
    @Test
    void get_returnsDTO_whenFound() {
        when(orderRepo.findById(1)).thenReturn(Optional.of(order1));
        when(orderMapper.toDto(order1)).thenReturn(dto1);

        OrderDTO out = service.get(1);

        assertThat(out).isEqualTo(dto1);
        verify(orderRepo).findById(1);
        verify(orderMapper).toDto(order1);
    }

    @Test
    void get_throws_whenNotFound() {
        when(orderRepo.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(404))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepo).findById(404);
        verifyNoInteractions(orderMapper);
    }

    // ---------------------------------------------------------------------
    // create(dto)
    // ---------------------------------------------------------------------
    @Test
    void create_saves_andReturnsDTO() {
        OrderDTO input = OrderDTO.builder()
                .customerId(cust1.getId())
                .storeId(store1.getId())
                .status("NEW")
                .build();

        when(customerRepo.findById(cust1.getId())).thenReturn(Optional.of(cust1));
        when(storeRepo.findById(store1.getId())).thenReturn(Optional.of(store1));
        // capture saved order to verify fields
        ArgumentCaptor<Order> saveCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepo.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(dto1);

        OrderDTO out = service.create(input);

        assertThat(out).isEqualTo(dto1);
        verify(customerRepo).findById(cust1.getId());
        verify(storeRepo).findById(store1.getId());
        verify(orderRepo).save(saveCaptor.capture());
        Order saved = saveCaptor.getValue();
        assertThat(saved.getCustomer()).isEqualTo(cust1);
        assertThat(saved.getStore()).isEqualTo(store1);
        assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(saved.getOrderTms()).isNotNull();
        verify(orderMapper).toDto(saved);
    }

    @Test
    void create_throws_whenRequiredFieldsMissing() {
        OrderDTO input = OrderDTO.builder().customerId(null).storeId(null).status(null).build();

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("customerId, storeId and status are required");

        verifyNoInteractions(customerRepo, storeRepo, orderRepo, orderMapper);
    }

    @Test
    void create_throws_whenInvalidStatus() {
        OrderDTO input = OrderDTO.builder()
                .customerId(cust1.getId()).storeId(store1.getId())
                .status("WRONG")
                .build();

        when(customerRepo.findById(cust1.getId())).thenReturn(Optional.of(cust1));
        when(storeRepo.findById(store1.getId())).thenReturn(Optional.of(store1));

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid order status");
    }

    @Test
    void create_throws_whenCustomerNotFound() {
        OrderDTO input = OrderDTO.builder()
                .customerId(999).storeId(store1.getId()).status("NEW").build();

        when(customerRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found");

        verify(customerRepo).findById(999);
        verifyNoInteractions(storeRepo, orderRepo, orderMapper);
    }

    @Test
    void create_throws_whenStoreNotFound() {
        OrderDTO input = OrderDTO.builder()
                .customerId(cust1.getId()).storeId(999).status("NEW").build();

        when(customerRepo.findById(cust1.getId())).thenReturn(Optional.of(cust1));
        when(storeRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Store not found");

        verify(customerRepo).findById(cust1.getId());
        verify(storeRepo).findById(999);
        verifyNoInteractions(orderRepo, orderMapper);
    }

    // ---------------------------------------------------------------------
    // update(id, dto)
    // ---------------------------------------------------------------------
    @Test
    void update_updatesStatus_andTms_andRelations_whenProvided() {
        Order existing = new Order();
        existing.setId(1);
        existing.setOrderStatus(OrderStatus.NEW);
        existing.setOrderTms(Instant.parse("2025-01-01T00:00:00Z"));
        existing.setCustomer(cust1);
        existing.setStore(store1);

        Customer cust2 = Customer.builder().id(200).fullName("John").emailAddress("john@example.com").build();
        Store store2 = new Store();
        store2.setId(20);
        store2.setStoreName("Outlet");
        store2.setWebAddress("https://outlet.example.com");

        OrderDTO patch = OrderDTO.builder()
                .status("COMPLETE")
                .orderTms(Instant.parse("2025-01-03T03:04:05Z"))
                .customerId(cust2.getId())
                .storeId(store2.getId())
                .build();

        Order saved = new Order();
        saved.setId(1);
        saved.setOrderStatus(OrderStatus.COMPLETE);
        saved.setOrderTms(patch.getOrderTms());
        saved.setCustomer(cust2);
        saved.setStore(store2);

        OrderDTO outDto = OrderDTO.builder().id(1).status("COMPLETE").build();

        when(orderRepo.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepo.findById(cust2.getId())).thenReturn(Optional.of(cust2));
        when(storeRepo.findById(store2.getId())).thenReturn(Optional.of(store2));
        when(orderRepo.save(existing)).thenReturn(saved);
        when(orderMapper.toDto(saved)).thenReturn(outDto);

        OrderDTO out = service.update(1, patch);

        assertThat(out).isEqualTo(outDto);
        assertThat(existing.getOrderStatus()).isEqualTo(OrderStatus.COMPLETE);
        assertThat(existing.getOrderTms()).isEqualTo(patch.getOrderTms());
        assertThat(existing.getCustomer()).isEqualTo(cust2);
        assertThat(existing.getStore()).isEqualTo(store2);
        verify(orderRepo).findById(1);
        verify(customerRepo).findById(cust2.getId());
        verify(storeRepo).findById(store2.getId());
        verify(orderRepo).save(existing);
        verify(orderMapper).toDto(saved);
    }

    @Test
    void update_throws_whenInvalidStatus() {
        when(orderRepo.findById(1)).thenReturn(Optional.of(order1));
        OrderDTO patch = OrderDTO.builder().status("BAD_STATUS").build();

        assertThatThrownBy(() -> service.update(1, patch))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid order status");

        verify(orderRepo).findById(1);
        verifyNoMoreInteractions(orderRepo);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void update_throws_whenOrderNotFound() {
        when(orderRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999, OrderDTO.builder().build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepo).findById(999);
    }

    @Test
    void update_throws_whenCustomerNotFound_onReassign() {
        when(orderRepo.findById(1)).thenReturn(Optional.of(order1));
        when(customerRepo.findById(999)).thenReturn(Optional.empty());

        OrderDTO patch = OrderDTO.builder().customerId(999).build();

        assertThatThrownBy(() -> service.update(1, patch))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void update_throws_whenStoreNotFound_onReassign() {
        when(orderRepo.findById(1)).thenReturn(Optional.of(order1));
        when(storeRepo.findById(999)).thenReturn(Optional.empty());

        OrderDTO patch = OrderDTO.builder().storeId(999).build();

        assertThatThrownBy(() -> service.update(1, patch))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Store not found");
    }

    // ---------------------------------------------------------------------
    // delete(id)
    // ---------------------------------------------------------------------
    @Test
    void delete_deletes_whenExists() {
        when(orderRepo.existsById(1)).thenReturn(true);

        service.delete(1);

        verify(orderRepo).existsById(1);
        verify(orderRepo).deleteById(1);
    }

    @Test
    void delete_throws_whenNotFound() {
        when(orderRepo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepo).existsById(99);
        verify(orderRepo, never()).deleteById(anyInt());
    }

    // ---------------------------------------------------------------------
    // countByStatus()
    // ---------------------------------------------------------------------
    @Test
    void countByStatus_mapsToStringLong() {
        List<Object[]> rows = List.of(
                new Object[]{OrderStatus.NEW, 3L},
                new Object[]{OrderStatus.COMPLETE, 7}
        );
        when(orderRepo.countOrdersByStatus()).thenReturn(rows);

        Map<String, Long> out = service.countByStatus();

        assertThat(out).containsEntry("NEW", 3L)
                       .containsEntry("COMPLETE", 7L)
                       .hasSize(2);
        verify(orderRepo).countOrdersByStatus();
    }

    // ---------------------------------------------------------------------
    // byStatus(status)
    // ---------------------------------------------------------------------
    @Test
    void byStatus_returnsMappedList_whenValidStatus() {
        when(orderRepo.findByOrderStatus(OrderStatus.NEW)).thenReturn(List.of(order1));
        when(orderMapper.toDto(order1)).thenReturn(dto1);

        List<OrderDTO> out = service.byStatus("new");

        assertThat(out).containsExactly(dto1);
        verify(orderRepo).findByOrderStatus(OrderStatus.NEW);
    }

    @Test
    void byStatus_throws_whenInvalidStatus() {
        assertThatThrownBy(() -> service.byStatus("oops"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status");

        verifyNoInteractions(orderRepo);
    }

    // ---------------------------------------------------------------------
    // byCustomer(customerId) / byCustomerRequired(customerId)
    // ---------------------------------------------------------------------
    @Test
    void byCustomer_returnsMappedList() {
        when(orderRepo.findByCustomerId(100)).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        List<OrderDTO> out = service.byCustomer(100);

        assertThat(out).containsExactly(dto1, dto2);
        verify(orderRepo).findByCustomerId(100);
    }

    @Test
    void byCustomerRequired_returns_whenNonEmpty_elseThrows() {
        when(orderRepo.findByCustomerId(100)).thenReturn(List.of(order1));
        when(orderMapper.toDto(order1)).thenReturn(dto1);

        List<OrderDTO> ok = service.byCustomerRequired(100);
        assertThat(ok).containsExactly(dto1);

        when(orderRepo.findByCustomerId(999)).thenReturn(List.of());
        assertThatThrownBy(() -> service.byCustomerRequired(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("customer ID not found");
    }

    // ---------------------------------------------------------------------
    // byCustomerEmail(email)
    // ---------------------------------------------------------------------
    @Test
    void byCustomerEmail_returnsMappedList() {
        when(orderRepo.findByCustomerEmail("veda@example.com")).thenReturn(List.of(order1));
        when(orderMapper.toDto(order1)).thenReturn(dto1);

        List<OrderDTO> out = service.byCustomerEmail("veda@example.com");

        assertThat(out).containsExactly(dto1);
        verify(orderRepo).findByCustomerEmail("veda@example.com");
    }

    // ---------------------------------------------------------------------
    // byStoreName(storeName)
    // ---------------------------------------------------------------------
    @Test
    void byStoreName_returnsMappedList() {
        when(orderRepo.findByStoreName("Main Store")).thenReturn(List.of(order1));
        when(orderMapper.toDto(order1)).thenReturn(dto1);

        List<OrderDTO> out = service.byStoreName("Main Store");

        assertThat(out).containsExactly(dto1);
        verify(orderRepo).findByStoreName("Main Store");
    }

    // ---------------------------------------------------------------------
    // byDateRange(startDate, endDate)
    // ---------------------------------------------------------------------
    @Test
    void byDateRange_returnsMappedList_whenValidDates_andStartLeEnd() {
        when(orderRepo.findByDateRange(any(Instant.class), any(Instant.class))).thenReturn(List.of(order1, order2));
        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        List<OrderDTO> out = service.byDateRange("2025-01-01", "2025-01-02");

        assertThat(out).containsExactly(dto1, dto2);

        // Verify date conversion roughly called
        ArgumentCaptor<Instant> startCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> endCap = ArgumentCaptor.forClass(Instant.class);
        verify(orderRepo).findByDateRange(startCap.capture(), endCap.capture());
        assertThat(startCap.getValue()).isBeforeOrEqualTo(endCap.getValue());
    }

    @Test
    void byDateRange_throws_whenBadFormat() {
        assertThatThrownBy(() -> service.byDateRange("2025/01/01", "2025-01-02"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("yyyy-MM-dd");
        verifyNoInteractions(orderRepo);
    }

    @Test
    void byDateRange_throws_whenStartAfterEnd() {
        assertThatThrownBy(() -> service.byDateRange("2025-01-03", "2025-01-02"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("startDate must be <="); // message as in service
        verifyNoInteractions(orderRepo);
    }

    // ---------------------------------------------------------------------
    // details(orderId)
    // ---------------------------------------------------------------------
    @Test
    void details_returnsComposedDetails() {
        // Arrange
        Order o = new Order();
        o.setId(1);
        o.setStore(store1); // assumes store1 is initialized in @BeforeEach with name "Main Store"

        // Expected DTOs from mapper
        LineItem li1 = LineItem.builder()
                .lineItemId(11).productId(100).productName("Phone X")
                .unitPrice(new BigDecimal("199.99")).quantity(2)
                .shipmentStatus("PENDING")
                .build();

        LineItem li2 = LineItem.builder()
                .lineItemId(12).productId(200).productName("Laptop Z")
                .unitPrice(new BigDecimal("999.00")).quantity(1)
                .shipmentStatus(null)
                .build();

        when(orderRepo.findById(1)).thenReturn(Optional.of(o));

        // ✅ Real domain objects instead of casting Object -> OrderItem
        com.order.inventory.entity.OrderItem oi1 = new com.order.inventory.entity.OrderItem();
        oi1.setId(11);
        oi1.setOrder(o);
        com.order.inventory.entity.Product p1 = new com.order.inventory.entity.Product();
        p1.setId(100);
        p1.setProductName("Phone X");
        oi1.setProduct(p1);
        oi1.setUnitPrice(new BigDecimal("199.99"));
        oi1.setQuantity(2);
        com.order.inventory.entity.Shipment shp = new com.order.inventory.entity.Shipment();
        shp.setShipmentStatus(com.order.inventory.entity.ShipmentStatus.PENDING);
        oi1.setShipment(shp);

        com.order.inventory.entity.OrderItem oi2 = new com.order.inventory.entity.OrderItem();
        oi2.setId(12);
        oi2.setOrder(o);
        com.order.inventory.entity.Product p2 = new com.order.inventory.entity.Product();
        p2.setId(200);
        p2.setProductName("Laptop Z");
        oi2.setProduct(p2);
        oi2.setUnitPrice(new BigDecimal("999.00"));
        oi2.setQuantity(1);
        oi2.setShipment(null);

        when(itemRepo.findByOrderId(1)).thenReturn(java.util.List.of(oi1, oi2));
        when(itemMapper.toLineItem(oi1)).thenReturn(li1);
        when(itemMapper.toLineItem(oi2)).thenReturn(li2);
        // 199.99*2 + 999.00 = 1398.98
        when(itemRepo.totalAmountByOrderId(1)).thenReturn(new BigDecimal("1398.98"));

        // Act
        OrderDTO.Details out = service.details(1);

        // Assert
        assertThat(out.getOrderId()).isEqualTo(1);
        assertThat(out.getStoreName()).isEqualTo("Main Store");
        assertThat(out.getItems()).containsExactly(li1, li2);
        assertThat(out.getShipmentStatus()).isEqualTo("PENDING"); // first non-null in items
        assertThat(out.getTotalAmount()).isEqualByComparingTo("1398.98");

        verify(orderRepo).findById(1);
        verify(itemRepo).findByOrderId(1);
        verify(itemMapper).toLineItem(oi1);
        verify(itemMapper).toLineItem(oi2);
        verify(itemRepo).totalAmountByOrderId(1);
    }
    
    @Test
    void details_throws_whenOrderMissing() {
        when(orderRepo.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.details(404))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepo).findById(404);
        verifyNoInteractions(itemRepo, itemMapper);
    }

    // ---------------------------------------------------------------------
    // cancel(id)
    // ---------------------------------------------------------------------
    @Test
    void cancel_setsCancelled_andReturnsDTO() {
        Order existing = new Order();
        existing.setId(1);
        existing.setOrderStatus(OrderStatus.NEW);

        when(orderRepo.findById(1)).thenReturn(Optional.of(existing));
        when(orderRepo.save(existing)).thenReturn(existing);

        OrderDTO cancelledDto = OrderDTO.builder().id(1).status("CANCELLED").build();
        when(orderMapper.toDto(existing)).thenReturn(cancelledDto);

        OrderDTO out = service.cancel(1);

        assertThat(existing.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(out).isEqualTo(cancelledDto);

        verify(orderRepo).findById(1);
        verify(orderRepo).save(existing);
        verify(orderMapper).toDto(existing);
    }

    @Test
    void cancel_throws_whenNotFound() {
        when(orderRepo.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepo).findById(999);
        verifyNoInteractions(orderMapper);
    }
}