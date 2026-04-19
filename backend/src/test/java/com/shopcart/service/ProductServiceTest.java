package com.shopcart.service;

import com.shopcart.entity.Product;
import com.shopcart.entity.enums.ProductStatus;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.repository.ProductRepository;
import com.shopcart.service.service.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService using Lombok + JUnit 5 + Mockito
 * 
 * Demonstrates:
 * - Using Lombok @Builder for elegant test data creation
 * - Nested test classes for organizing test scenarios
 * - Parameterized test approach using test data builders
 * - Clean test setup and assertions with Lombok getters
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IInventoryService inventoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    /**
     * Test data builders for different product scenarios
     */
    private Product activeProduct;
    private Product inactiveProduct;

    @BeforeEach
    void setUp() {
        // Lombok @Builder makes test data creation incredibly readable
        activeProduct = Product.builder()
                .id("PROD-001")
                .name("Active Product")
                .price(100000L)
                .description("A test product")
                .status(ProductStatus.ACTIVE)
                .build();

        inactiveProduct = Product.builder()
                .id("PROD-002")
                .name("Inactive Product")
                .price(50000L)
                .description("An inactive test product")
                .status(ProductStatus.INACTIVE)
                .build();

        log.debug("Test products created: {} and {}", activeProduct.getId(), inactiveProduct.getId());
    }

    @Nested
    @DisplayName("Get Product Tests")
    class GetProductTests {

        @Test
        @DisplayName("Should get all products successfully")
        void testGetAllProducts_Success() {
            // Arrange
            List<Product> products = Arrays.asList(activeProduct, inactiveProduct);
            when(productRepository.findAll()).thenReturn(products);

            // Act
            List<Product> result = productService.getAllProducts();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream()
                    .anyMatch(p -> p.getId().equals("PROD-001")));

            // Verify interaction
            verify(productRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should get product by id successfully")
        void testGetProductById_Success() {
            // Arrange
            when(productRepository.findById("PROD-001")).thenReturn(Optional.of(activeProduct));

            // Act
            Product result = productService.getProductById("PROD-001");

            // Assert
            assertNotNull(result);
            assertEquals("PROD-001", result.getId());
            assertEquals("Active Product", result.getName());
            assertEquals(ProductStatus.ACTIVE, result.getStatus());

            // With Lombok @Data, all getters/setters work perfectly
            assertEquals(100000L, result.getPrice());
            assertEquals("A test product", result.getDescription());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void testGetProductById_NotFound() {
            // Arrange
            when(productRepository.findById("INVALID-ID")).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.getProductById("INVALID-ID")
            );

            assertEquals("Product not found with id: INVALID-ID", exception.getMessage());
            verify(productRepository).findById("INVALID-ID");
        }
    }

    @Nested
    @DisplayName("Search Product Tests")
    class SearchProductTests {

        @Test
        @DisplayName("Should search products by name successfully")
        void testSearchProductsByName_Success() {
            // Arrange
            List<Product> allProducts = Arrays.asList(
                    activeProduct,
                    Product.builder()
                            .id("PROD-003")
                            .name("Another Active")
                            .price(200000L)
                            .status(ProductStatus.ACTIVE)
                            .build(),
                    inactiveProduct
            );
            
            when(productRepository.findAll()).thenReturn(allProducts);

            // Act
            List<Product> results = productService.searchProductsByName("Active");

            // Assert
            assertEquals(3, results.size());
            assertTrue(results.stream().allMatch(p -> p.getName().toLowerCase().contains("active")));
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void testSearchProductsByName_NoMatches() {
            // Arrange
            List<Product> allProducts = Arrays.asList(activeProduct, inactiveProduct);
            when(productRepository.findAll()).thenReturn(allProducts);

            // Act
            List<Product> results = productService.searchProductsByName("NonExistent");

            // Assert
            assertEquals(0, results.size());
        }

        @Test
        @DisplayName("Should perform case-insensitive search")
        void testSearchProductsByName_CaseInsensitive() {
            // Arrange
            List<Product> allProducts = Arrays.asList(activeProduct, inactiveProduct);
            when(productRepository.findAll()).thenReturn(allProducts);

            // Act
            List<Product> results = productService.searchProductsByName("active");

            // Assert
            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(p -> p.getName().toLowerCase().contains("active")));
        }
    }

    @Nested
    @DisplayName("Inventory Tests")
    class InventoryTests {

        @Test
        @DisplayName("Should get available stock for product")
        void testGetAvailableStock_Success() {
            // Arrange
            String productId = "PROD-001";
            Integer expectedStock = 50;
            
            when(productRepository.findById(productId)).thenReturn(Optional.of(activeProduct));
            when(inventoryService.getStock(productId)).thenReturn(expectedStock);

            // Act
            Integer result = productService.getAvailableStock(productId);

            // Assert
            assertEquals(expectedStock, result);
            verify(productRepository).findById(productId);
            verify(inventoryService).getStock(productId);
        }

        @Test
        @DisplayName("Should check product availability")
        void testIsProductAvailable_True() {
            // Arrange
            String productId = "PROD-001";
            when(inventoryService.hasEnoughStock(productId, 1)).thenReturn(true);

            // Act
            boolean result = productService.isProductAvailable(productId);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when product not available")
        void testIsProductAvailable_False() {
            // Arrange
            String productId = "PROD-001";
            when(inventoryService.hasEnoughStock(productId, 1)).thenReturn(false);

            // Act
            boolean result = productService.isProductAvailable(productId);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Product Comparison Tests")
    class ProductComparisonTests {

        @Test
        @DisplayName("Should compare two products using Lombok equals/hashCode")
        void testProductEquality() {
            // Arrange - Create two products with same data
            Product product1 = Product.builder()
                    .id("PROD-001")
                    .name("Test")
                    .price(100L)
                    .status(ProductStatus.ACTIVE)
                    .build();

            Product product2 = Product.builder()
                    .id("PROD-001")
                    .name("Test")
                    .price(100L)
                    .status(ProductStatus.ACTIVE)
                    .build();

            // Act & Assert - Lombok @Data generates equals() automatically
            assertEquals(product1, product2);
            assertEquals(product1.hashCode(), product2.hashCode());
        }

        @Test
        @DisplayName("Should print product using Lombok toString")
        void testProductToString() {
            // Act
            String productString = activeProduct.toString();

            // Assert - Lombok @Data generates toString() with all fields
            assertTrue(productString.contains("PROD-001"));
            assertTrue(productString.contains("Active Product"));
            assertTrue(productString.contains("100000"));

            log.debug("Product toString(): {}", productString);
        }
    }
}
