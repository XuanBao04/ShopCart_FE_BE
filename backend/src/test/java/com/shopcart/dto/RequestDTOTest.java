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

    @Test
    @DisplayName("Should build OrderRequest with shipping address fields")
    void testOrderRequestWithShippingAddress() {
        // Arrange - Build order with address
        List<OrderItemRequest> items = Arrays.asList(
                OrderItemRequest.builder()
                        .productId("PROD-001")
                        .quantity(1)
                        .price(100000L)
                        .build()
        );

        // Act
        OrderRequest request = OrderRequest.builder()
                .userId("user-address-001")
                .orderItems(items)
                .shippingAddress("123 Đường Nguyễn Huệ")
                .city("TP Hồ Chí Minh")
                .district("Quận 1")
                .ward("Phường Bến Nghé")
                .postalCode("700000")
                .phoneNumber("0912345678")
                .build();

        // Assert
        assertEquals("user-address-001", request.getUserId());
        assertEquals("123 Đường Nguyễn Huệ", request.getShippingAddress());
        assertEquals("TP Hồ Chí Minh", request.getCity());
        assertEquals("Quận 1", request.getDistrict());
        assertEquals("Phường Bến Nghé", request.getWard());
        assertEquals("700000", request.getPostalCode());
        assertEquals("0912345678", request.getPhoneNumber());

        log.info("OrderRequest with address: {}", request);
    }

    @Test
    @DisplayName("Should build OrderRequest with optional postal code null")
    void testOrderRequestOptionalPostalCode() {
        // Arrange
        List<OrderItemRequest> items = Arrays.asList(
                OrderItemRequest.builder()
                        .productId("PROD-002")
                        .quantity(2)
                        .price(50000L)
                        .build()
        );

        // Act
        OrderRequest request = OrderRequest.builder()
                .userId("user-address-002")
                .orderItems(items)
                .shippingAddress("456 Đường Lê Lợi")
                .city("Hà Nội")
                .district("Quận Ba Đình")
                .ward("Phường Cát Linh")
                .postalCode(null)
                .phoneNumber("0987654321")
                .build();

        // Assert
        assertNull(request.getPostalCode());
        assertEquals("user-address-002", request.getUserId());
        assertEquals("456 Đường Lê Lợi", request.getShippingAddress());
        assertEquals("0987654321", request.getPhoneNumber());

        log.info("OrderRequest without postal code: {}", request);
    }

    @Test
    @DisplayName("Should test OrderRequest address fields equality")
    void testOrderRequestAddressEquality() {
        // Arrange
        List<OrderItemRequest> items = Arrays.asList(
                OrderItemRequest.builder()
                        .productId("PROD-001")
                        .quantity(1)
                        .price(100000L)
                        .build()
        );

        OrderRequest request1 = OrderRequest.builder()
                .userId("user-001")
                .orderItems(items)
                .shippingAddress("123 Đường A")
                .city("TP HCM")
                .district("Q1")
                .ward("P1")
                .postalCode("700000")
                .phoneNumber("0912345678")
                .build();

        OrderRequest request2 = OrderRequest.builder()
                .userId("user-001")
                .orderItems(items)
                .shippingAddress("123 Đường A")
                .city("TP HCM")
                .district("Q1")
                .ward("P1")
                .postalCode("700000")
                .phoneNumber("0912345678")
                .build();

        OrderRequest request3 = OrderRequest.builder()
                .userId("user-001")
                .orderItems(items)
                .shippingAddress("456 Đường B")  // Different address
                .city("TP HCM")
                .district("Q1")
                .ward("P1")
                .postalCode("700000")
                .phoneNumber("0912345678")
                .build();

        // Assert - Same address fields should be equal
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        
        // Different address should not be equal
        assertNotEquals(request1, request3);
    }

    @Test
    @DisplayName("Should handle OrderRequest with coupon and address")
    void testOrderRequestWithCouponAndAddress() {
        // Arrange
        List<OrderItemRequest> items = Arrays.asList(
                OrderItemRequest.builder()
                        .productId("PROD-001")
                        .quantity(1)
                        .price(100000L)
                        .build()
        );

        // Act
        OrderRequest request = OrderRequest.builder()
                .userId("user-coupon-001")
                .orderItems(items)
                .couponCode("SUMMER2024")
                .shippingAddress("789 Đường C")
                .city("Đà Nẵng")
                .district("Quận Hải Châu")
                .ward("Phường Thanh Bình")
                .postalCode("500000")
                .phoneNumber("0901234567")
                .build();

        // Assert
        assertEquals("user-coupon-001", request.getUserId());
        assertEquals("SUMMER2024", request.getCouponCode());
        assertEquals("789 Đường C", request.getShippingAddress());
        assertEquals("Đà Nẵng", request.getCity());
        assertEquals("0901234567", request.getPhoneNumber());

        log.info("OrderRequest with coupon and address: {}", request);
    }
}
