package com.shopcart.dto;

import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.dto.response.CartResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class demonstrating DTO Builder patterns for testing
 * 
 * Demonstrates:
 * - Builder pattern for test data construction
 * - Fluent/chained builder methods for readable test setup
 * - Complex object composition using builders
 */
@Slf4j
@DisplayName("DTO Builder Patterns for Testing")
class DTOBuilderPatternsTest {

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
