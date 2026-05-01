package com.shopcart.dto;

import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.dto.response.CartResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.dto.response.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Response DTO models using Lombok
 * 
 * Demonstrates:
 * - Using Lombok @Builder for building Response DTOs in tests
 * - Testing Response DTO equality/hashCode
 * - Testing Response DTO toString() method
 * - Testing Response DTO constructors (no-args, all-args)
 * - Building nested Response objects
 */
@Slf4j
@DisplayName("Response DTO Tests")
class ResponseDTOTest {

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
                .createdAt(now)
                .lastModifiedDate(now)
                .build();

        // Assert
        assertEquals("ORD-001", response.getId());
        assertEquals("user-001", response.getUserId());
        assertEquals("PENDING", response.getStatus());
        assertNotNull(response.getCreatedAt());
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
        LocalDateTime now = LocalDateTime.now();
        CartItemResponse response = new CartItemResponse(5L, "PROD-005", 10, 50000L, 500000L, now);

        // Assert
        assertEquals(5L, response.getId());
        assertEquals("PROD-005", response.getProductId());
        assertEquals(10, response.getQuantity());
        assertEquals(50000L, response.getPrice());
        assertEquals(500000L, response.getTotalPrice());
        assertEquals(now, response.getCreatedAt());
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

    @Test
    @DisplayName("Should build OrderResponse with shipping address fields")
    void testOrderResponseWithShippingAddress() {
        // Arrange & Act
        OrderResponse response = OrderResponse.builder()
                .id("ORD-ADDR-001")
                .userId("user-001")
                .items(new ArrayList<>())
                .subtotal(100000L)
                .discountAmount(10000L)
                .shippingFee(29900L)
                .totalPrice(119900L)
                .couponCode("SUMMER10")
                .status("PENDING")
                .shippingAddress("123 Đường Nguyễn Huệ")
                .city("TP Hồ Chí Minh")
                .district("Quận 1")
                .ward("Phường Bến Nghé")
                .postalCode("700000")
                .phoneNumber("0912345678")
                .build();

        // Assert
        assertEquals("ORD-ADDR-001", response.getId());
        assertEquals("user-001", response.getUserId());
        assertEquals("123 Đường Nguyễn Huệ", response.getShippingAddress());
        assertEquals("TP Hồ Chí Minh", response.getCity());
        assertEquals("Quận 1", response.getDistrict());
        assertEquals("Phường Bến Nghé", response.getWard());
        assertEquals("700000", response.getPostalCode());
        assertEquals("0912345678", response.getPhoneNumber());
        assertEquals(119900L, response.getTotalPrice());

        log.info("OrderResponse with address: {}", response);
    }

    @Test
    @DisplayName("Should handle optional postal code in OrderResponse")
    void testOrderResponseOptionalPostalCode() {
        // Arrange & Act
        OrderResponse response = OrderResponse.builder()
                .id("ORD-ADDR-002")
                .userId("user-002")
                .items(new ArrayList<>())
                .subtotal(50000L)
                .discountAmount(0L)
                .shippingFee(29900L)
                .totalPrice(79900L)
                .status("PENDING")
                .shippingAddress("456 Đường Lê Lợi")
                .city("Hà Nội")
                .district("Quận Ba Đình")
                .ward("Phường Cát Linh")
                .postalCode(null)
                .phoneNumber("0987654321")
                .build();

        // Assert
        assertNull(response.getPostalCode());
        assertNotNull(response.getShippingAddress());
        assertEquals("0987654321", response.getPhoneNumber());

        log.info("OrderResponse without postal code: {}", response);
    }

    @Test
    @DisplayName("Should test OrderResponse address fields equality")
    void testOrderResponseAddressEquality() {
        // Arrange
        OrderResponse response1 = OrderResponse.builder()
                .id("ORD-ADDR-003")
                .userId("user-003")
                .items(new ArrayList<>())
                .subtotal(100000L)
                .discountAmount(0L)
                .shippingFee(29900L)
                .totalPrice(129900L)
                .status("PENDING")
                .shippingAddress("789 Đường A")
                .city("Đà Nẵng")
                .district("Quận Hải Châu")
                .ward("Phường Thanh Bình")
                .postalCode("500000")
                .phoneNumber("0901234567")
                .build();

        OrderResponse response2 = OrderResponse.builder()
                .id("ORD-ADDR-003")
                .userId("user-003")
                .items(new ArrayList<>())
                .subtotal(100000L)
                .discountAmount(0L)
                .shippingFee(29900L)
                .totalPrice(129900L)
                .status("PENDING")
                .shippingAddress("789 Đường A")
                .city("Đà Nẵng")
                .district("Quận Hải Châu")
                .ward("Phường Thanh Bình")
                .postalCode("500000")
                .phoneNumber("0901234567")
                .build();

        OrderResponse response3 = OrderResponse.builder()
                .id("ORD-ADDR-003")
                .userId("user-003")
                .items(new ArrayList<>())
                .subtotal(100000L)
                .discountAmount(0L)
                .shippingFee(29900L)
                .totalPrice(129900L)
                .status("PENDING")
                .shippingAddress("999 Đường B")  // Different address
                .city("Đà Nẵng")
                .district("Quận Hải Châu")
                .ward("Phường Thanh Bình")
                .postalCode("500000")
                .phoneNumber("0901234567")
                .build();

        // Assert - Same address should be equal
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
        
        // Different address should not be equal
        assertNotEquals(response1, response3);

        log.info("OrderResponse address equality verified");
    }
}
