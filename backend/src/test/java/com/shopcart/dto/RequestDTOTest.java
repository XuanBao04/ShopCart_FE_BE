package com.shopcart.dto;

import com.shopcart.dto.request.CartItemRequest;
import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Request DTO models using Lombok
 * 
 * Demonstrates:
 * - Using Lombok @Builder for building Request DTOs in tests
 * - Testing Request DTO equality/hashCode
 * - Testing Request DTO constructors (no-args, all-args)
 * - Building nested Request objects
 */
@Slf4j
@DisplayName("Request DTO Tests")
class RequestDTOTest {

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
