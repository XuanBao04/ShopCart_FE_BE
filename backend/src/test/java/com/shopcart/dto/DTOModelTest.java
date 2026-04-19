package com.shopcart.dto;

import com.shopcart.dto.request.CartItemRequest;
import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.dto.response.CartResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.dto.response.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DTO models using Lombok
 * 
 * Demonstrates:
 * - Using Lombok @Builder for building DTOs in tests
 * - Testing DTO equality/hashCode
 * - Testing DTO serialization scenarios
 * - Clean test data construction for integration tests
 */
@Slf4j
@DisplayName("DTO Model Tests")
class DTOModelTest {

    @Nested
    @DisplayName("Request DTO Tests")
    class RequestDTOTests {

        @Test
        @DisplayName("Should build CartItemRequest using Lombok Builder")
        void testCartItemRequestBuilder() {
            // Arrange & Act - Using Lombok @Builder for clean construction
            CartItemRequest request = CartItemRequest.builder()
                    .productId("PROD-001")
                    .quantity(5)
                    .build();

            // Assert - Lombok @Data provides getters
            assertEquals("PROD-001", request.getProductId());
            assertEquals(5, request.getQuantity());

            log.info("CartItemRequest: {}", request);
        }

        @Test
        @DisplayName("Should test CartItemRequest equality with Lombok")
        void testCartItemRequestEquality() {
            // Arrange
            CartItemRequest request1 = CartItemRequest.builder()
                    .productId("PROD-001")
                    .quantity(5)
                    .build();

            CartItemRequest request2 = CartItemRequest.builder()
                    .productId("PROD-001")
                    .quantity(5)
                    .build();

            CartItemRequest request3 = CartItemRequest.builder()
                    .productId("PROD-002")
                    .quantity(5)
                    .build();

            // Assert - Lombok generates equals() checking all fields
            assertEquals(request1, request2);
            assertEquals(request1.hashCode(), request2.hashCode());
            assertNotEquals(request1, request3);
        }

        @Test
        @DisplayName("Should build OrderItemRequest with all fields")
        void testOrderItemRequestBuilder() {
            // Arrange & Act
            OrderItemRequest request = OrderItemRequest.builder()
                    .productId("PROD-001")
                    .quantity(2)
                    .price(100000L)
                    .build();

            // Assert
            assertEquals("PROD-001", request.getProductId());
            assertEquals(2, request.getQuantity());
            assertEquals(100000L, request.getPrice());

            log.debug("OrderItemRequest: {}", request);
        }

        @Test
        @DisplayName("Should build OrderRequest with nested OrderItemRequests")
        void testOrderRequestBuilderWithNested() {
            // Arrange - Build nested items using Lombok
            List<OrderItemRequest> items = Arrays.asList(
                    OrderItemRequest.builder()
                            .productId("PROD-001")
                            .quantity(2)
                            .price(100000L)
                            .build(),
                    OrderItemRequest.builder()
                            .productId("PROD-002")
                            .quantity(1)
                            .price(50000L)
                            .build()
            );

            // Act
            OrderRequest request = OrderRequest.builder()
                    .userId("user-001")
                    .orderItems(items)
                    .build();

            // Assert
            assertEquals("user-001", request.getUserId());
            assertEquals(2, request.getOrderItems().size());
            assertEquals(100000L, request.getOrderItems().get(0).getPrice());

            log.info("OrderRequest with items: {}", request);
        }

        @Test
        @DisplayName("Should test OrderRequest with empty items list")
        void testOrderRequestEmptyItems() {
            // Arrange & Act
            OrderRequest request = OrderRequest.builder()
                    .userId("user-002")
                    .orderItems(new ArrayList<>())
                    .build();

            // Assert
            assertEquals("user-002", request.getUserId());
            assertTrue(request.getOrderItems().isEmpty());
        }

        @Test
        @DisplayName("Should test no-args constructor for Request DTOs")
        void testRequestNoArgsConstructor() {
            // Arrange
            CartItemRequest request = new CartItemRequest();

            // Act
            request.setProductId("PROD-TEST");
            request.setQuantity(10);

            // Assert
            assertEquals("PROD-TEST", request.getProductId());
            assertEquals(10, request.getQuantity());

            log.debug("CartItemRequest from no-args: {}", request);
        }

        @Test
        @DisplayName("Should test all-args constructor for Request DTOs")
        void testRequestAllArgsConstructor() {
            // Act
            CartItemRequest request = new CartItemRequest("PROD-ALL", 20);

            // Assert
            assertEquals("PROD-ALL", request.getProductId());
            assertEquals(20, request.getQuantity());
        }
    }

    @Nested
    @DisplayName("Response DTO Tests")
    class ResponseDTOTests {

        @Test
        @DisplayName("Should build CartItemResponse using Lombok Builder")
        void testCartItemResponseBuilder() {
            // Arrange & Act
            CartItemResponse response = CartItemResponse.builder()
                    .id(1L)
                    .productId("PROD-001")
                    .quantity(5)
                    .build();

            // Assert
            assertEquals(1L, response.getId());
            assertEquals("PROD-001", response.getProductId());
            assertEquals(5, response.getQuantity());

            log.info("CartItemResponse: {}", response);
        }

        @Test
        @DisplayName("Should build CartResponse with nested items")
        void testCartResponseWithItems() {
            // Arrange - Build nested items
            List<CartItemResponse> items = Arrays.asList(
                    CartItemResponse.builder()
                            .id(1L)
                            .productId("PROD-001")
                            .quantity(2)
                            .build(),
                    CartItemResponse.builder()
                            .id(2L)
                            .productId("PROD-002")
                            .quantity(3)
                            .build()
            );

            // Act
            CartResponse response = CartResponse.builder()
                    .userId("user-001")
                    .items(items)
                    .totalItems(5)
                    .totalPrice(250000L)
                    .build();

            // Assert
            assertEquals("user-001", response.getUserId());
            assertEquals(2, response.getItems().size());
            assertEquals(5, response.getTotalItems());
            assertEquals(250000L, response.getTotalPrice());

            log.info("CartResponse: {}", response);
        }

        @Test
        @DisplayName("Should build OrderResponse with timestamps")
        void testOrderResponseWithTimestamps() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            OrderResponse response = OrderResponse.builder()
                    .id("ORD-001")
                    .userId("user-001")
                    .items(new ArrayList<>())
                    .status("PENDING")
                    .createdDate(now)
                    .lastModifiedDate(now)
                    .build();

            // Assert
            assertEquals("ORD-001", response.getId());
            assertEquals("user-001", response.getUserId());
            assertEquals("PENDING", response.getStatus());
            assertNotNull(response.getCreatedDate());
            assertNotNull(response.getLastModifiedDate());

            log.debug("OrderResponse: {}", response);
        }

        @Test
        @DisplayName("Should build ProductResponse for API responses")
        void testProductResponseBuilder() {
            // Act
            ProductResponse response = ProductResponse.builder()
                    .id("PROD-001")
                    .name("Test Product")
                    .description("A quality product")
                    .price(100000L)
                    .status("ACTIVE")
                    .build();

            // Assert
            assertEquals("PROD-001", response.getId());
            assertEquals("Test Product", response.getName());
            assertEquals("A quality product", response.getDescription());
            assertEquals(100000L, response.getPrice());
            assertEquals("ACTIVE", response.getStatus());

            log.info("ProductResponse: {}", response);
        }

        @Test
        @DisplayName("Should test response DTO equality")
        void testResponseDTOEquality() {
            // Arrange
            CartItemResponse resp1 = CartItemResponse.builder()
                    .id(1L)
                    .productId("PROD-001")
                    .quantity(5)
                    .build();

            CartItemResponse resp2 = CartItemResponse.builder()
                    .id(1L)
                    .productId("PROD-001")
                    .quantity(5)
                    .build();

            // Assert
            assertEquals(resp1, resp2);
            assertEquals(resp1.hashCode(), resp2.hashCode());
        }

        @Test
        @DisplayName("Should test Lombok toString for response DTOs")
        void testResponseDTOToString() {
            // Arrange
            OrderResponse response = OrderResponse.builder()
                    .id("ORD-123")
                    .userId("user-456")
                    .status("SHIPPED")
                    .build();

            // Act
            String responseString = response.toString();

            // Assert - Lombok generates toString with all fields
            assertTrue(responseString.contains("ORD-123"));
            assertTrue(responseString.contains("user-456"));
            assertTrue(responseString.contains("SHIPPED"));

            log.info("Response toString: {}", responseString);
        }

        @Test
        @DisplayName("Should test all-args constructor for Response DTOs")
        void testResponseAllArgsConstructor() {
            // Act
            CartItemResponse response = new CartItemResponse(5L, "PROD-005", 10);

            // Assert
            assertEquals(5L, response.getId());
            assertEquals("PROD-005", response.getProductId());
            assertEquals(10, response.getQuantity());
        }

        @Test
        @DisplayName("Should test no-args constructor for Response DTOs")
        void testResponseNoArgsConstructor() {
            // Arrange
            ProductResponse response = new ProductResponse();

            // Act
            response.setId("PROD-NEW");
            response.setName("New Product");
            response.setPrice(50000L);
            response.setStatus("ACTIVE");

            // Assert
            assertEquals("PROD-NEW", response.getId());
            assertEquals("New Product", response.getName());
            assertEquals(50000L, response.getPrice());
        }
    }

    @Nested
    @DisplayName("DTO Builder Patterns for Testing")
    class DTOBuilderPatterns {

        @Test
        @DisplayName("Should demonstrate builder pattern for test data construction")
        void testBuilderPatternForTestData() {
            // This pattern is excellent for creating complex test scenarios
            
            // Create multiple order items efficiently
            List<OrderItemRequest> items = Arrays.asList(
                    OrderItemRequest.builder().productId("P1").quantity(1).price(10000L).build(),
                    OrderItemRequest.builder().productId("P2").quantity(2).price(20000L).build(),
                    OrderItemRequest.builder().productId("P3").quantity(3).price(30000L).build()
            );

            // Create order request
            OrderRequest orderRequest = OrderRequest.builder()
                    .userId("user-test")
                    .orderItems(items)
                    .build();

            // Assert
            assertEquals(3, orderRequest.getOrderItems().size());
            
            // Verify all items were created correctly
            for (int i = 0; i < items.size(); i++) {
                assertEquals(i + 1, items.get(i).getQuantity());
            }

            log.info("Order with {} items created", orderRequest.getOrderItems().size());
        }

        @Test
        @DisplayName("Should chain builder methods for readable test setup")
        void testChainedBuilderMethods() {
            // Arrange - Build in a single fluent chain (thanks to Lombok!)
            CartResponse cartResponse = CartResponse.builder()
                    .userId("chain-user")
                    .items(Arrays.asList(
                            CartItemResponse.builder().id(1L).productId("P1").quantity(5).build(),
                            CartItemResponse.builder().id(2L).productId("P2").quantity(10).build()
                    ))
                    .totalItems(15)
                    .totalPrice(150000L)
                    .build();

            // Assert
            assertEquals("chain-user", cartResponse.getUserId());
            assertEquals(2, cartResponse.getItems().size());
            assertEquals(150000L, cartResponse.getTotalPrice());
        }
    }
}
