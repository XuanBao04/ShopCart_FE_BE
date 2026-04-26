package com.shopcart.entity;

import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.entity.enums.ProductStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Entity models using Lombok
 * 
 * Demonstrates:
 * - Using Lombok @Builder for building complex objects
 * - Lombok @Data providing equals/hashCode/toString
 * - Entity relationship testing
 * - Lombok constructor generation
 */
@Slf4j
@DisplayName("Entity Model Tests")
class EntityModelTest {

    private Product product;
    private CartItem cartItem;
    private Order order;
    private OrderItem orderItem;
    private Inventory inventory;

    /**
     * Setup test data using Lombok @Builder - Much cleaner than new Keyword!
     * Before Lombok: new CartItem("user1", "prod1", 5);
     * After Lombok: CartItem.builder().userId("user1").productId("prod1").quantity(5).build();
     */
    @BeforeEach
    void setUp() {
        log.debug("Setting up test entities using Lombok @Builder");

        // Product creation with Lombok @Builder
        product = Product.builder()
                .id("PROD-001")
                .name("Test Product")
                .price(50000L)
                .description("A high-quality test product")
                .status(ProductStatus.ACTIVE)
                .build();

        // CartItem creation with Lombok @Builder
        cartItem = CartItem.builder()
                .id(1L)
                .userId("user-001")
                .productId("PROD-001")
                .quantity(3)
                .build();

        // Order creation with Lombok @Builder
        order = Order.builder()
                .id("ORD-001")
                .userId("user-001")
                .totalPrice(150000L)
                .shippingFee(10000L)
                .couponCode("SAVE10")
                .status(OrderStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        // OrderItem creation with Lombok @Builder
        orderItem = OrderItem.builder()
                .id(1L)
                .order(order)
                .productId("PROD-001")
                .quantity(2)
                .price(50000L)
                .build();

        // Inventory creation with Lombok @Builder
        inventory = Inventory.builder()
                .id(1L)
                .productId("PROD-001")
                .quantity(100)
                .build();
    }

    @Test
    @DisplayName("Should create Product with all fields using Lombok Builder")
    void testProductBuilder() {
        // Assert - Lombok @Data provides getters automatically
        assertEquals("PROD-001", product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals(50000L, product.getPrice());
        assertEquals("A high-quality test product", product.getDescription());
        assertEquals(ProductStatus.ACTIVE, product.getStatus());

        log.info("Product created: {}", product);
    }

    @Test
    @DisplayName("Should create CartItem and verify Lombok getters/setters")
    void testCartItemBuilderAndSetters() {
        // Arrange
        CartItem item = CartItem.builder()
                .userId("user-002")
                .productId("PROD-002")
                .quantity(5)
                .build();

        // Act - Lombok provides setters
        item.setQuantity(10);

        // Assert - Lombok provides getters
        assertEquals("user-002", item.getUserId());
        assertEquals("PROD-002", item.getProductId());
        assertEquals(10, item.getQuantity());

        log.info("CartItem after update: {}", item);
    }

    @Test
    @DisplayName("Should test Lombok equals/hashCode for CartItem")
    void testCartItemEquality() {
        // Arrange - Create two CartItems with same data
        CartItem item1 = CartItem.builder()
                .id(1L)
                .userId("user-001")
                .productId("PROD-001")
                .quantity(5)
                .build();

        CartItem item2 = CartItem.builder()
                .id(1L)
                .userId("user-001")
                .productId("PROD-001")
                .quantity(5)
                .build();

        CartItem item3 = CartItem.builder()
                .id(2L)
                .userId("user-001")
                .productId("PROD-001")
                .quantity(5)
                .build();

        // Assert - Lombok @Data generates equals() using all fields
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1, item3);
    }

    @Test
    @DisplayName("Should create Order with relationships using Lombok")
    void testOrderWithOrderItems() {
        // Arrange
        Order newOrder = Order.builder()
                .id("ORD-002")
                .userId("user-002")
                .totalPrice(200000L)
                .status(OrderStatus.PROCESSING)
                .createdDate(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        OrderItem item1 = OrderItem.builder()
                .id(10L)
                .order(newOrder)
                .productId("PROD-001")
                .quantity(2)
                .price(100000L)
                .build();

        OrderItem item2 = OrderItem.builder()
                .id(11L)
                .order(newOrder)
                .productId("PROD-002")
                .quantity(1)
                .price(100000L)
                .build();

        // Act
        newOrder.getOrderItems().add(item1);
        newOrder.getOrderItems().add(item2);

        // Assert
        assertEquals(2, newOrder.getOrderItems().size());
        assertTrue(newOrder.getOrderItems().stream()
                .anyMatch(oi -> oi.getProductId().equals("PROD-001")));

        log.info("Order with items: {}", newOrder);
    }

    @Test
    @DisplayName("Should test Inventory stock management")
    void testInventoryStockManagement() {
        // Arrange
        Inventory inv = Inventory.builder()
                .id(5L)
                .productId("PROD-005")
                .quantity(100)
                .build();

        // Act - Reduce stock
        inv.setQuantity(inv.getQuantity() - 10);

        // Assert
        assertEquals(90, inv.getQuantity());

        // Act - Increase stock
        inv.setQuantity(inv.getQuantity() + 5);

        // Assert
        assertEquals(95, inv.getQuantity());

        log.debug("Inventory after updates: {}", inv);
    }

    @Test
    @DisplayName("Should test Order status progression")
    void testOrderStatusProgression() {
        // Arrange
        Order newOrder = Order.builder()
                .id("ORD-003")
                .userId("user-003")
                .totalPrice(100000L)
                .status(OrderStatus.PENDING)
                .createdDate(LocalDateTime.now())
                .build();

        // Act - Progress order through statuses
        assertEquals(OrderStatus.PENDING, newOrder.getStatus());

        newOrder.setStatus(OrderStatus.PROCESSING);
        assertEquals(OrderStatus.PROCESSING, newOrder.getStatus());

        newOrder.setStatus(OrderStatus.SHIPPED);
        assertEquals(OrderStatus.SHIPPED, newOrder.getStatus());

        newOrder.setStatus(OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, newOrder.getStatus());

        log.info("Order progression completed: {}", newOrder);
    }

    @Test
    @DisplayName("Should test Lombok no-arg constructor")
    void testNoArgsConstructor() {
        // Arrange - Using no-arg constructor
        CartItem item = new CartItem();

        // Act - Using setters
        item.setUserId("user-new");
        item.setProductId("PROD-new");
        item.setQuantity(1);

        // Assert
        assertEquals("user-new", item.getUserId());
        assertEquals("PROD-new", item.getProductId());
        assertEquals(1, item.getQuantity());

        log.debug("CartItem from no-args constructor: {}", item);
    }

    @Test
    @DisplayName("Should test Lombok all-args constructor")
    void testAllArgsConstructor() {
        // Arrange - Using all-args constructor
        LocalDateTime now = LocalDateTime.now();
        CartItem item = new CartItem(100L, "user-100", "PROD-100", 5, now);

        // Assert
        assertEquals(100L, item.getId());
        assertEquals("user-100", item.getUserId());
        assertEquals("PROD-100", item.getProductId());
        assertEquals(5, item.getQuantity());
        assertEquals(now, item.getCreatedAt());

        log.debug("CartItem from all-args constructor: {}", item);
    }

    @Test
    @DisplayName("Should test Lombok toString for debugging")
    void testLombokToString() {
        // Act
        String productString = product.toString();
        String orderString = order.toString();

        // Assert - Lombok @Data generates toString() with all fields
        assertTrue(productString.contains("id"));
        assertTrue(productString.contains("name"));
        assertTrue(productString.contains("PROD-001"));

        assertTrue(orderString.contains("userId"));
        assertTrue(orderString.contains("ORD-001"));

        log.info("Product toString: {}", productString);
        log.info("Order toString: {}", orderString);
    }

    @Test
    @DisplayName("Should compare products with different fields")
    void testProductInequality() {
        // Arrange
        Product prod1 = Product.builder()
                .id("PROD-X")
                .name("Product X")
                .price(100L)
                .status(ProductStatus.ACTIVE)
                .build();

        Product prod2 = Product.builder()
                .id("PROD-X")
                .name("Product X Modified")  // Different name
                .price(100L)
                .status(ProductStatus.ACTIVE)
                .build();

        // Assert - Lombok equals() checks all fields
        assertNotEquals(prod1, prod2);
    }

    @Test
    @DisplayName("Should work with Lombok @Builder.Default annotation on collections")
    void testOrderDefaultOrderItems() {
        // Arrange
        Order newOrder = Order.builder()
                .id("ORD-EMPTY")
                .userId("user-empty")
                .totalPrice(0L)
                .status(OrderStatus.PENDING)
                .build();

        // Assert - Should have empty list (due to @Builder.Default)
        assertNotNull(newOrder.getOrderItems());
        assertTrue(newOrder.getOrderItems().isEmpty());

        log.info("Order with default items list: {}", newOrder);
    }
}
