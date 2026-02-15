package com.inventory.service;

import com.order.inventory.dto.ProductDTO;
import com.order.inventory.entity.Product;
import com.order.inventory.exception.BadRequestException;
import com.order.inventory.exception.NotFoundException;
import com.order.inventory.mapper.ProductMapper;
import com.order.inventory.repository.ProductRepository;
import com.order.inventory.service.ProductService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repo;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    // Fixtures
    private Product p1;
    private Product p2;
    private ProductDTO d1;
    private ProductDTO d2;

    @BeforeEach
    void setUp() {
        p1 = Product.builder()
                .id(1)
                .productName("Phone X")
                .unitPrice(new BigDecimal("199.99"))
                .brand("ACME")
                .colour("Red")
                .size("M")
                .rating(4)
                .build();

        p2 = Product.builder()
                .id(2)
                .productName("Laptop Z")
                .unitPrice(new BigDecimal("999.00"))
                .brand("ZETA")
                .colour("Blue")
                .size("L")
                .rating(5)
                .build();

        d1 = ProductDTO.builder()
                .id(1).name("Phone X").unitPrice(new BigDecimal("199.99"))
                .brand("ACME").colour("Red").size("M").rating(4)
                .build();

        d2 = ProductDTO.builder()
                .id(2).name("Laptop Z").unitPrice(new BigDecimal("999.00"))
                .brand("ZETA").colour("Blue").size("L").rating(5)
                .build();
    }

    // ---------------------------------------------------------------------
    // getAll(sortField)
    // ---------------------------------------------------------------------

    @Test
    void getAll_unsorted_whenSortFieldBlank() {
        when(repo.findAll(Sort.unsorted())).thenReturn(List.of(p1, p2));
        when(mapper.toDto(p1)).thenReturn(d1);
        when(mapper.toDto(p2)).thenReturn(d2);

        List<ProductDTO> out = service.getAll("  ");

        assertThat(out).containsExactly(d1, d2);
        verify(repo).findAll(Sort.unsorted());
        verify(mapper).toDto(p1);
        verify(mapper).toDto(p2);
    }

    @Test
    void getAll_sorts_whenFieldAllowed() {
        Sort expected = Sort.by("unitPrice").ascending();
        when(repo.findAll(expected)).thenReturn(List.of(p1, p2));
        when(mapper.toDto(p1)).thenReturn(d1);
        when(mapper.toDto(p2)).thenReturn(d2);

        List<ProductDTO> out = service.getAll("unitPrice");

        assertThat(out).containsExactly(d1, d2);
        verify(repo).findAll(expected);
    }

    @Test
    void getAll_throws_whenFieldNotAllowed() {
        assertThatThrownBy(() -> service.getAll("unknownField"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid sort field");
        verifyNoInteractions(repo, mapper);
    }

    // ---------------------------------------------------------------------
    // getAllStrict(field)
    // ---------------------------------------------------------------------

    @Test
    void getAllStrict_returns_whenValidField() {
        Sort expected = Sort.by("brand").ascending();
        when(repo.findAll(expected)).thenReturn(List.of(p1));
        when(mapper.toDto(p1)).thenReturn(d1);

        List<ProductDTO> out = service.getAllStrict("brand");

        assertThat(out).containsExactly(d1);
        verify(repo).findAll(expected);
    }

    @Test
    void getAllStrict_throws_whenNullOrBlankOrInvalid() {
        assertThatThrownBy(() -> service.getAllStrict(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid sort field");
        assertThatThrownBy(() -> service.getAllStrict("  "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid sort field");
        assertThatThrownBy(() -> service.getAllStrict("oops"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid sort field");

        verifyNoInteractions(repo, mapper);
    }

    // ---------------------------------------------------------------------
    // create(dto)
    // ---------------------------------------------------------------------

    @Test
    void create_saves_andReturnsDTO() {
        ProductDTO input = ProductDTO.builder()
                .name("Mouse A")
                .unitPrice(new BigDecimal("49.99"))
                .brand("ACME")
                .colour("Black")
                .size("S")
                .rating(3)
                .build();

        Product toSave = Product.builder()
                .id(null)
                .productName("Mouse A")
                .unitPrice(new BigDecimal("49.99"))
                .brand("ACME")
                .colour("Black")
                .size("S")
                .rating(3)
                .build();

        Product saved = Product.builder()
                .id(10)
                .productName("Mouse A")
                .unitPrice(new BigDecimal("49.99"))
                .brand("ACME")
                .colour("Black")
                .size("S")
                .rating(3)
                .build();

        ProductDTO outDto = ProductDTO.builder()
                .id(10)
                .name("Mouse A")
                .unitPrice(new BigDecimal("49.99"))
                .brand("ACME")
                .colour("Black")
                .size("S")
                .rating(3)
                .build();

        when(mapper.toEntity(input)).thenReturn(toSave);
        when(repo.save(toSave)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(outDto);

        ProductDTO out = service.create(input);

        assertThat(out).isEqualTo(outDto);
        verify(mapper).toEntity(input);
        verify(repo).save(toSave);
        verify(mapper).toDto(saved);
    }

    @Test
    void create_throws_whenNameMissingOrBlank_orPriceMissing() {
        ProductDTO noName = ProductDTO.builder().name(null).unitPrice(new BigDecimal("1.00")).build();
        ProductDTO blankName = ProductDTO.builder().name(" ").unitPrice(new BigDecimal("1.00")).build();
        ProductDTO noPrice = ProductDTO.builder().name("ABC").unitPrice(null).build();

        assertThatThrownBy(() -> service.create(noName))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Name and unitPrice are required");
        assertThatThrownBy(() -> service.create(blankName))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Name and unitPrice are required");
        assertThatThrownBy(() -> service.create(noPrice))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Name and unitPrice are required");

        verifyNoInteractions(repo, mapper);
    }

    @Test
    void create_throws_whenPriceNegative() {
        ProductDTO input = ProductDTO.builder()
                .name("Phone X")
                .unitPrice(new BigDecimal("-0.01"))
                .build();

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unitPrice cannot be negative");

        verifyNoInteractions(repo, mapper);
    }

    // ---------------------------------------------------------------------
    // update(id, dto)
    // ---------------------------------------------------------------------

    @Test
    void update_appliesProvidedFields_andReturnsDTO() {
        Product existing = Product.builder()
                .id(1)
                .productName("Old Name")
                .brand("OldBrand")
                .colour("OldColour")
                .size("M")
                .rating(3)
                .unitPrice(new BigDecimal("10.00"))
                .build();

        ProductDTO patch = ProductDTO.builder()
                .name("New Name")
                .brand("NewBrand")
                .colour("NewColour")
                .size("L")
                .rating(5)
                .unitPrice(new BigDecimal("12.34"))
                .build();

        Product saved = Product.builder()
                .id(1)
                .productName("New Name")
                .brand("NewBrand")
                .colour("NewColour")
                .size("L")
                .rating(5)
                .unitPrice(new BigDecimal("12.34"))
                .build();

        ProductDTO outDto = ProductDTO.builder()
                .id(1).name("New Name").brand("NewBrand").colour("NewColour")
                .size("L").rating(5).unitPrice(new BigDecimal("12.34"))
                .build();

        when(repo.findById(1)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(outDto);

        ProductDTO out = service.update(1, patch);

        assertThat(out).isEqualTo(outDto);
        assertThat(existing.getProductName()).isEqualTo("New Name");
        assertThat(existing.getBrand()).isEqualTo("NewBrand");
        assertThat(existing.getColour()).isEqualTo("NewColour");
        assertThat(existing.getSize()).isEqualTo("L");
        assertThat(existing.getRating()).isEqualTo(5);
        assertThat(existing.getUnitPrice()).isEqualByComparingTo("12.34");

        verify(repo).findById(1);
        verify(repo).save(existing);
        verify(mapper).toDto(saved);
    }

    @Test
    void update_throws_whenProductNotFound() {
        when(repo.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(404, ProductDTO.builder().build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(repo).findById(404);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(mapper);
    }

    @Test
    void update_throws_whenNewPriceNegative() {
        Product existing = Product.builder()
                .id(1)
                .productName("Name")
                .unitPrice(new BigDecimal("10.00"))
                .build();

        when(repo.findById(1)).thenReturn(Optional.of(existing));

        ProductDTO patch = ProductDTO.builder()
                .unitPrice(new BigDecimal("-1.00"))
                .build();

        assertThatThrownBy(() -> service.update(1, patch))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unitPrice cannot be negative");

        verify(repo).findById(1);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------------------
    // delete(id)
    // ---------------------------------------------------------------------

    @Test
    void delete_deletes_whenExists() {
        when(repo.existsById(1)).thenReturn(true);

        service.delete(1);

        verify(repo).existsById(1);
        verify(repo).deleteById(1);
    }

    @Test
    void delete_throws_whenNotFound() {
        when(repo.existsById(99)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(repo).existsById(99);
        verify(repo, never()).deleteById(anyInt());
    }

    // ---------------------------------------------------------------------
    // byBrand(brand)
    // ---------------------------------------------------------------------

    @Test
    void byBrand_returnsMappedList_whenFound() {
        when(repo.findByBrandIgnoreCase("ACME")).thenReturn(List.of(p1));
        when(mapper.toDto(p1)).thenReturn(d1);

        List<ProductDTO> out = service.byBrand("ACME");

        assertThat(out).containsExactly(d1);
        verify(repo).findByBrandIgnoreCase("ACME");
        verify(mapper).toDto(p1);
    }

    @Test
    void byBrand_throws_whenEmpty() {
        when(repo.findByBrandIgnoreCase("UNKNOWN")).thenReturn(List.of());

        assertThatThrownBy(() -> service.byBrand("UNKNOWN"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No products found for brand: UNKNOWN");

        verify(repo).findByBrandIgnoreCase("UNKNOWN");
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------------------
    // byColour(colour)
    // ---------------------------------------------------------------------

    @Test
    void byColour_returnsMappedList_whenFound() {
        when(repo.findByColourIgnoreCase("Blue")).thenReturn(List.of(p2));
        when(mapper.toDto(p2)).thenReturn(d2);

        List<ProductDTO> out = service.byColour("Blue");

        assertThat(out).containsExactly(d2);
        verify(repo).findByColourIgnoreCase("Blue");
        verify(mapper).toDto(p2);
    }

    @Test
    void byColour_throws_whenEmpty() {
        when(repo.findByColourIgnoreCase("Purple")).thenReturn(List.of());

        assertThatThrownBy(() -> service.byColour("Purple"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No products found for colour: Purple");

        verify(repo).findByColourIgnoreCase("Purple");
        verifyNoInteractions(mapper);
    }

    // ---------------------------------------------------------------------
    // byPrice(min, max)
    // ---------------------------------------------------------------------

    @Test
    void byPrice_returnsMappedList_whenValidRange() {
        BigDecimal min = new BigDecimal("100.00");
        BigDecimal max = new BigDecimal("1000.00");

        when(repo.findByUnitPriceBetween(min, max)).thenReturn(List.of(p1, p2));
        when(mapper.toDto(p1)).thenReturn(d1);
        when(mapper.toDto(p2)).thenReturn(d2);

        List<ProductDTO> out = service.byPrice(min, max);

        assertThat(out).containsExactly(d1, d2);
        verify(repo).findByUnitPriceBetween(min, max);
    }

    @Test
    void byPrice_throws_whenNulls_orMinGreaterThanMax() {
        assertThatThrownBy(() -> service.byPrice(null, new BigDecimal("1.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid min/max price");

        assertThatThrownBy(() -> service.byPrice(new BigDecimal("1.00"), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid min/max price");

        assertThatThrownBy(() -> service.byPrice(new BigDecimal("10.00"), new BigDecimal("5.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid min/max price");

        verifyNoInteractions(repo, mapper);
    }

    @Test
    void byPrice_throws_whenMinNegative() {
        assertThatThrownBy(() -> service.byPrice(new BigDecimal("-0.01"), new BigDecimal("10.00")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("min cannot be negative");

        verifyNoInteractions(repo, mapper);
    }

    // ---------------------------------------------------------------------
    // byName(name)
    // ---------------------------------------------------------------------

    @Test
    void byName_returnsMappedList_whenFound() {
        when(repo.searchByName("phone")).thenReturn(List.of(p1));
        when(mapper.toDto(p1)).thenReturn(d1);

        List<ProductDTO> out = service.byName("phone");

        assertThat(out).containsExactly(d1);
        verify(repo).searchByName("phone");
        verify(mapper).toDto(p1);
    }

    @Test
    void byName_throws_whenEmpty() {
        when(repo.searchByName("nope")).thenReturn(List.of());

        assertThatThrownBy(() -> service.byName("nope"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No products found matching name: nope");

        verify(repo).searchByName("nope");
        verifyNoInteractions(mapper);
    }
}