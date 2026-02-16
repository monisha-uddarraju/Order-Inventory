package com.inventory.service;
import com.order.inventory.dto.CustomerDTO;

import com.order.inventory.dto.ShipmentDTO;

import com.order.inventory.entity.Customer;

import com.order.inventory.entity.OrderStatus;

import com.order.inventory.entity.ShipmentStatus;

import com.order.inventory.exception.BadRequestException;

import com.order.inventory.exception.NotFoundException;

import com.order.inventory.mapper.CustomerMapper;

import com.order.inventory.repository.CustomerRepository;

import com.order.inventory.repository.ShipmentRepository;
import com.order.inventory.service.CustomerService;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Nested;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
/**

* Unit tests for CustomerService (JUnit 5, Mockito).

* No AssertJ usage as requested.

*/

@ExtendWith(MockitoExtension.class)

class CustomerServiceTest {
   @Mock

   private CustomerRepository customerRepo;
   @Mock

   private ShipmentRepository shipmentRepo;
   @Mock

   private CustomerMapper mapper;
   @InjectMocks

   private CustomerService service;
   private Customer customer1;

   private Customer customer2;

   private CustomerDTO dto1;

   private CustomerDTO dto2;
   @BeforeEach

   void setUp() {

       customer1 = Customer.builder()

               .id(1)

               .emailAddress("alice@example.com")

               .fullName("Alice Wonderland")

               .build();
       customer2 = Customer.builder()

               .id(2)

               .emailAddress("bob@example.com")

               .fullName("Bob Builder")

               .build();
       dto1 = CustomerDTO.builder()

               .id(1)

               .email("alice@example.com")

               .fullName("Alice Wonderland")

               .build();
       dto2 = CustomerDTO.builder()

               .id(2)

               .email("bob@example.com")

               .fullName("Bob Builder")

               .build();

   }
   // ---------------------------------------------------------

   // CRUD

   // ---------------------------------------------------------
   @Test

   @DisplayName("all() returns mapped DTO list")

   void all_returnsMappedList() {

       when(customerRepo.findAll()).thenReturn(List.of(customer1, customer2));

       when(mapper.toDto(customer1)).thenReturn(dto1);

       when(mapper.toDto(customer2)).thenReturn(dto2);
       List<CustomerDTO> out = service.all();
       assertEquals(2, out.size());

       assertEquals("alice@example.com", out.get(0).getEmail());

       assertEquals("Bob Builder", out.get(1).getFullName());

       verify(customerRepo, times(1)).findAll();

       verify(mapper, times(2)).toDto(any(Customer.class));

   }
   @Test

   @DisplayName("create() succeeds with valid input and nullifies id before save")

   void create_success() {

       CustomerDTO input = CustomerDTO.builder()

               .id(999) // should be ignored

               .email("new.user@example.com")

               .fullName("New User")

               .build();
       // mapper.toEntity returns entity with some id (service should nullify it)

       Customer mapped = Customer.builder()

               .id(999) // will be nullified by service

               .emailAddress("new.user@example.com")

               .fullName("New User")

               .build();
       Customer saved = Customer.builder()

               .id(10)

               .emailAddress("new.user@example.com")

               .fullName("New User")

               .build();
       CustomerDTO savedDto = CustomerDTO.builder()

               .id(10)

               .email("new.user@example.com")

               .fullName("New User")

               .build();
       when(mapper.toEntity(input)).thenReturn(mapped);

       when(customerRepo.save(any(Customer.class))).thenReturn(saved);

       when(mapper.toDto(saved)).thenReturn(savedDto);
       CustomerDTO out = service.create(input);
       assertEquals(10, out.getId());

       assertEquals("new.user@example.com", out.getEmail());

       assertEquals("New User", out.getFullName());
       ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);

       verify(customerRepo).save(captor.capture());

       Customer toSave = captor.getValue();

       assertNull(toSave.getId(), "Service must nullify ID before saving");

   }
   @Test

   @DisplayName("create() rejects null email")

   void create_rejectsNullEmail() {

       CustomerDTO input = CustomerDTO.builder()

               .email(null)

               .fullName("X")

               .build();

       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(input));

       assertEquals("Email is required", ex.getMessage());

   }
   @Test

   @DisplayName("create() rejects blank email")

   void create_rejectsBlankEmail() {

       CustomerDTO input = CustomerDTO.builder()

               .email("  ")

               .fullName("X")

               .build();

       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(input));

       assertEquals("Email is required", ex.getMessage());

   }
   @Test

   @DisplayName("create() rejects invalid email format")

   void create_rejectsInvalidEmailFormat() {

       CustomerDTO input = CustomerDTO.builder()

               .email("invalid-at-example")

               .fullName("X")

               .build();

       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(input));

       assertEquals("Invalid email format", ex.getMessage());

   }
   @Test

   @DisplayName("create() rejects missing fullName")

   void create_rejectsMissingFullName() {

       CustomerDTO input = CustomerDTO.builder()

               .email("ok@example.com")

               .fullName(" ")

               .build();

       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(input));

       assertEquals("fullName is required", ex.getMessage());

   }
   @Test

   @DisplayName("update() updates email and name when present")

   void update_success() {

       CustomerDTO patch = CustomerDTO.builder()

               .email("newalice@example.com")

               .fullName("Alice W.")

               .build();
       Customer saved = Customer.builder()

               .id(1)

               .emailAddress("newalice@example.com")

               .fullName("Alice W.")

               .build();
       CustomerDTO savedDto = CustomerDTO.builder()

               .id(1)

               .email("newalice@example.com")

               .fullName("Alice W.")

               .build();
       when(customerRepo.findById(1)).thenReturn(Optional.of(customer1));

       when(customerRepo.save(customer1)).thenReturn(saved);

       when(mapper.toDto(saved)).thenReturn(savedDto);
       CustomerDTO out = service.update(1, patch);
       assertEquals(1, out.getId());

       assertEquals("newalice@example.com", out.getEmail());

       assertEquals("Alice W.", out.getFullName());
       assertEquals("newalice@example.com", customer1.getEmailAddress());

       assertEquals("Alice W.", customer1.getFullName());

       verify(customerRepo).save(customer1);

   }
   @Test

   @DisplayName("update() rejects invalid email format when provided")

   void update_rejectsInvalidEmail() {

       CustomerDTO patch = CustomerDTO.builder()

               .email("bad-email")

               .build();
       when(customerRepo.findById(1)).thenReturn(Optional.of(customer1));
       BadRequestException ex = assertThrows(BadRequestException.class, () -> service.update(1, patch));

       assertEquals("Invalid email format", ex.getMessage());

       verify(customerRepo, never()).save(any());

   }
   @Test

   @DisplayName("update() throws NotFound when id doesn't exist")

   void update_notFound() {

       when(customerRepo.findById(99)).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.update(99, new CustomerDTO()));

       assertEquals("Customer not found", ex.getMessage());

   }
   @Test

   @DisplayName("delete() deletes when exists")

   void delete_success() {

       when(customerRepo.existsById(1)).thenReturn(true);

       service.delete(1);

       verify(customerRepo).deleteById(1);

   }
   @Test

   @DisplayName("delete() throws NotFound when not exists")

   void delete_notFound() {

       when(customerRepo.existsById(99)).thenReturn(false);

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.delete(99));

       assertEquals("Customer not found", ex.getMessage());

       verify(customerRepo, never()).deleteById(anyInt());

   }
   // ---------------------------------------------------------

   // Search (email / name)

   // ---------------------------------------------------------
   @Test

   @DisplayName("byEmailRequired() returns singleton list when found")

   void byEmailRequired_found() {

       when(customerRepo.findByEmailAddress("alice@example.com")).thenReturn(Optional.of(customer1));

       when(mapper.toDto(customer1)).thenReturn(dto1);
       List<CustomerDTO> out = service.byEmailRequired("alice@example.com");
       assertEquals(1, out.size());

       assertEquals("alice@example.com", out.get(0).getEmail());

   }
   @Test

   @DisplayName("byEmailRequired() throws NotFound when missing")

   void byEmailRequired_notFound() {

       when(customerRepo.findByEmailAddress("missing@example.com")).thenReturn(Optional.empty());

       NotFoundException ex = assertThrows(NotFoundException.class,

               () -> service.byEmailRequired("missing@example.com"));

       assertEquals("Customer with the provided email ID not found.", ex.getMessage());

   }
   @Test

   @DisplayName("byNameWildcardRequired() returns mapped list when found")

   void byNameWildcardRequired_found() {

       when(customerRepo.searchByNameWildcard("ali")).thenReturn(List.of(customer1));

       when(mapper.toDto(customer1)).thenReturn(dto1);
       List<CustomerDTO> out = service.byNameWildcardRequired("ali");
       assertEquals(1, out.size());

       assertEquals("Alice Wonderland", out.get(0).getFullName());

   }
   @Test

   @DisplayName("byNameWildcardRequired() throws NotFound when empty")

   void byNameWildcardRequired_empty() {

       when(customerRepo.searchByNameWildcard("zzz")).thenReturn(List.of());

       NotFoundException ex = assertThrows(NotFoundException.class,

               () -> service.byNameWildcardRequired("zzz"));

       assertEquals("Customer with the provided name wildcard not found.", ex.getMessage());

   }
   // ---------------------------------------------------------

   // Shipment status wise count of customers

   // ---------------------------------------------------------
   @Test

   @DisplayName("shipmentStatusWiseCustomerCount() maps rows to status->count")

   void shipmentStatusWiseCustomerCount_mapsCorrectly() {

       List<Object[]> rows = List.of(

               new Object[]{ShipmentStatus.PENDING, 3L},

               new Object[]{ShipmentStatus.DELIVERED, 5}

       );

       when(shipmentRepo.countDistinctCustomersByShipmentStatus()).thenReturn(rows);
       Map<String, Long> out = service.shipmentStatusWiseCustomerCount();
       assertEquals(2, out.size());

       assertEquals(3L, out.get("PENDING"));

       assertEquals(5L, out.get("DELIVERED"));

   }
   // ---------------------------------------------------------

   // Customer shipments (404 if none)

   // ---------------------------------------------------------
   @Test

   @DisplayName("shipmentsByCustomerRequired() maps repository shipments to DTOs")

   void shipmentsByCustomerRequired_maps() {

       // We will mock Shipment + nested dependencies using Mockito.

       com.order.inventory.entity.Shipment sh1 = mock(com.order.inventory.entity.Shipment.class);

       com.order.inventory.entity.Shipment sh2 = mock(com.order.inventory.entity.Shipment.class);

       com.order.inventory.entity.Store store1 = mock(com.order.inventory.entity.Store.class);

       com.order.inventory.entity.Store store2 = mock(com.order.inventory.entity.Store.class);

       Customer cust = Customer.builder().id(77).emailAddress("x@y.com").fullName("X").build();
       when(sh1.getId()).thenReturn(1001);

       when(sh1.getStore()).thenReturn(store1);

       when(store1.getId()).thenReturn(201);

       when(sh1.getCustomer()).thenReturn(cust);

       when(sh1.getDeliveryAddress()).thenReturn("Addr-1");

       when(sh1.getShipmentStatus()).thenReturn(ShipmentStatus.PENDING);
       when(sh2.getId()).thenReturn(1002);

       when(sh2.getStore()).thenReturn(store2);

       when(store2.getId()).thenReturn(202);

       when(sh2.getCustomer()).thenReturn(cust);

       when(sh2.getDeliveryAddress()).thenReturn("Addr-2");

       when(sh2.getShipmentStatus()).thenReturn(ShipmentStatus.DELIVERED);
       when(shipmentRepo.findByCustomer_Id(77)).thenReturn(List.of(sh1, sh2));
       List<ShipmentDTO> out = service.shipmentsByCustomerRequired(77);
       assertEquals(2, out.size());
       ShipmentDTO d1 = out.get(0);

       assertEquals(1001, d1.getId());

       assertEquals(201, d1.getStoreId());

       assertEquals(77, d1.getCustomerId());

       assertEquals("Addr-1", d1.getDeliveryAddress());

       assertEquals("PENDING", d1.getStatus());
       ShipmentDTO d2 = out.get(1);

       assertEquals(1002, d2.getId());

       assertEquals(202, d2.getStoreId());

       assertEquals(77, d2.getCustomerId());

       assertEquals("Addr-2", d2.getDeliveryAddress());

       assertEquals("DELIVERED", d2.getStatus());

   }
   @Test

   @DisplayName("shipmentsByCustomerRequired() throws NotFound when none")

   void shipmentsByCustomerRequired_empty() {

       when(shipmentRepo.findByCustomer_Id(999)).thenReturn(List.of());

       NotFoundException ex = assertThrows(NotFoundException.class, () -> service.shipmentsByCustomerRequired(999));

       assertEquals("Shipment history for the specified customer ID not found.", ex.getMessage());

   }
   // ---------------------------------------------------------

   // Derived filters

   // ---------------------------------------------------------
   @Test

   @DisplayName("customersByShipmentStatus() returns list when not required and empty")

   void customersByShipmentStatus_notRequired_allowsEmpty() {

       when(customerRepo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of());

       List<CustomerDTO> out = service.customersByShipmentStatus(ShipmentStatus.PENDING, false);

       assertTrue(out.isEmpty());

   }
   @Test

   @DisplayName("customersByShipmentStatus() throws NotFound when required and empty")

   void customersByShipmentStatus_required_throwsWhenEmpty() {

       when(customerRepo.findCustomersByShipmentStatus(ShipmentStatus.PENDING)).thenReturn(List.of());

       NotFoundException ex = assertThrows(NotFoundException.class,

               () -> service.customersByShipmentStatus(ShipmentStatus.PENDING, true));

       assertEquals("No customers found with shipment status: PENDING", ex.getMessage());

   }
   @Test

   @DisplayName("customersByShipmentStatus() maps when results present")

   void customersByShipmentStatus_maps() {

       when(customerRepo.findCustomersByShipmentStatus(ShipmentStatus.DELIVERED))

               .thenReturn(List.of(customer1, customer2));

       when(mapper.toDto(customer1)).thenReturn(dto1);

       when(mapper.toDto(customer2)).thenReturn(dto2);
       List<CustomerDTO> out = service.customersByShipmentStatus(ShipmentStatus.DELIVERED, true);
       assertEquals(2, out.size());

       assertEquals("alice@example.com", out.get(0).getEmail());

       assertEquals("bob@example.com", out.get(1).getEmail());

   }
   @Test

   @DisplayName("customersWithCompletedOrdersRequired() returns mapped list")

   void customersWithCompletedOrdersRequired_found() {

       when(customerRepo.findCustomersWithOrderStatus(OrderStatus.COMPLETE))

               .thenReturn(List.of(customer1));

       when(mapper.toDto(customer1)).thenReturn(dto1);
       List<CustomerDTO> out = service.customersWithCompletedOrdersRequired();
       assertEquals(1, out.size());

       assertEquals("Alice Wonderland", out.get(0).getFullName());

   }
   @Test

   @DisplayName("customersWithCompletedOrdersRequired() throws NotFound on empty")

   void customersWithCompletedOrdersRequired_empty() {

       when(customerRepo.findCustomersWithOrderStatus(OrderStatus.COMPLETE))

               .thenReturn(List.of());

       NotFoundException ex = assertThrows(NotFoundException.class,

               service::customersWithCompletedOrdersRequired);

       assertEquals("No customers found with completed orders.", ex.getMessage());

   }
   @Nested

   @DisplayName("customersByOrderQuantityBetweenRequired() validation")

   class OrderQuantityBetweenValidation {
       @Test

       void rejectsNegativeMin() {

           BadRequestException ex = assertThrows(BadRequestException.class,

                   () -> service.customersByOrderQuantityBetweenRequired(-1, 10));

           assertEquals("Invalid request. Please provide valid minimum and maximum quantities for orders.", ex.getMessage());

       }
       @Test

       void rejectsNegativeMax() {

           BadRequestException ex = assertThrows(BadRequestException.class,

                   () -> service.customersByOrderQuantityBetweenRequired(0, -5));

           assertEquals("Invalid request. Please provide valid minimum and maximum quantities for orders.", ex.getMessage());

       }
       @Test

       void rejectsMinGreaterThanMax() {

           BadRequestException ex = assertThrows(BadRequestException.class,

                   () -> service.customersByOrderQuantityBetweenRequired(10, 5));

           assertEquals("Invalid request. Please provide valid minimum and maximum quantities for orders.", ex.getMessage());

       }

   }
   @Test

   @DisplayName("customersByOrderQuantityBetweenRequired() maps rows to DTOs")

   void customersByOrderQuantityBetweenRequired_maps() {

       // repo returns List<Object[]> where [0] is Customer, [1] is sum(quantity), but service only uses [0]

       List<Object[]> rows = List.of(

               new Object[]{customer1, 15L},

               new Object[]{customer2, 25}

       );
       when(customerRepo.findCustomersByTotalOrderedQuantityBetween(10, 30)).thenReturn(rows);

       when(mapper.toDto(customer1)).thenReturn(dto1);

       when(mapper.toDto(customer2)).thenReturn(dto2);
       List<CustomerDTO> out = service.customersByOrderQuantityBetweenRequired(10, 30);
       assertEquals(2, out.size());

       assertEquals(1, out.get(0).getId());

       assertEquals(2, out.get(1).getId());

   }
   @Test

   @DisplayName("customersByOrderQuantityBetweenRequired() throws NotFound when empty")

   void customersByOrderQuantityBetweenRequired_empty() {

       when(customerRepo.findCustomersByTotalOrderedQuantityBetween(1, 2)).thenReturn(List.of());

       NotFoundException ex = assertThrows(NotFoundException.class,

               () -> service.customersByOrderQuantityBetweenRequired(1, 2));

       assertEquals("No customers found in the specified order quantity range.", ex.getMessage());

   }

}

 