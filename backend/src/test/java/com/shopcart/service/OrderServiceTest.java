package com.shopcart.service;

import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderItemResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.Product;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.entity.enums.ProductStatus;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.mapper.OrderMapper;
import com.shopcart.repository.OrderRepository;
import com.shopcart.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderService using Lombok + JUnit 5 + Mockito
 * 
 * Tests cover:
 * - Order creation with shipping fee (29,900 VND default)
 * - Stock validation
 * - Order retrieval
 * - Order cancellation
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private IInventoryService inventoryService;

    @Mock
    private IProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private String testUserId;
    private Product testProduct;
    private OrderRequest testOrderRequest;
    private Order testOrder;
    private OrderResponse testOrderResponse;

    @BeforeEach
    void setUp() {
        testUserId = "user-001";

        // Test product
        testProduct = Product.builder()
                .id("PROD-001")
                .name("Test Product")
                .price(100000L)
                .description("A test product")
                .status(ProductStatus.ACTIVE)
                .build();

        // Test order request with one item
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId("PROD-001")
                .quantity(1)
                .price(100000L)
                .build();

        testOrderRequest = OrderRequest.builder()
                .userId(testUserId)
                .orderItems(Arrays.asList(itemRequest))
                .build();

        // Test order response
        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .productId("PROD-001")
                .quantity(1)
                .price(100000L)
                .build();

        testOrderResponse = OrderResponse.builder()
                .userId(testUserId)
                .items(Arrays.asList(itemResponse))
                .shippingFee(29900L)
                .totalPrice(129900L)
                .status("PENDING")
                .build();

        log.info("Test setup completed with userId: {}", testUserId);
    }

    @Nested
    @DisplayName("Order Creation Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully with default shipping fee")
        void testCreateOrder_Success() {
            // Arrange
            when(productService.getProductById("PROD-001")).thenReturn(testProduct);
            when(inventoryService.hasEnoughStock("PROD-001", 1)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId("ORD-001");
                order.setCreatedDate(LocalDateTime.now());
                order.setLastModifiedDate(LocalDateTime.now());
                return order;
            });
            when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(testOrderResponse);

            // Act
            OrderResponse result = orderService.createOrder(testOrderRequest);

            // Assert
            assertNotNull(result);
            assertEquals(testUserId, result.getUserId());
            assertEquals(29900L, result.getShippingFee());
            assertEquals(129900L, result.getTotalPrice()); // 100k + 29.9k
            assertEquals("PENDING", result.getStatus());

            // Verify mocks were called
            verify(productService).getProductById("PROD-001");
            verify(inventoryService).hasEnoughStock("PROD-001", 1);
            verify(orderRepository).save(any(Order.class));
            verify(orderMapper).toOrderResponse(any(Order.class));

            log.info("Order created successfully with shipping fee: 29,900 VND");
        }

        @Test
        @DisplayName("Should calculate total price including shipping fee")
        void testCreateOrder_TotalIncludesShipping() {
            // Arrange: Order with 2 items
            OrderItemRequest item1 = OrderItemRequest.builder()
                    .productId("PROD-001")
                    .quantity(2)
                    .price(50000L)
                    .build();

            OrderRequest multiItemRequest = OrderRequest.builder()
                    .userId(testUserId)
                    .orderItems(Arrays.asList(item1))
                    .build();

            when(productService.getProductById("PROD-001")).thenReturn(testProduct);
            when(inventoryService.hasEnoughStock("PROD-001", 2)).thenReturn(true);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            when(orderRepository.save(orderCaptor.capture())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return order;
            });
            when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                return OrderResponse.builder()
                        .shippingFee(order.getShippingFee())
                        .totalPrice(order.getTotalPrice())
                        .build();
            });

            // Act
            OrderResponse result = orderService.createOrder(multiItemRequest);

            // Assert
            Order capturedOrder = orderCaptor.getValue();
            long expectedSubtotal = 50000L * 2; // 100k
            long expectedTotal = expectedSubtotal + 29900L; // 129.9k

            assertEquals(29900L, capturedOrder.getShippingFee());
            assertEquals(expectedTotal, capturedOrder.getTotalPrice());
            assertEquals(expectedTotal, result.getTotalPrice());

            log.info("Order total calculated correctly: {} VND (subtotal: {} + shipping: 29,900)",
                    expectedTotal, expectedSubtotal);
        }

        @Test
        @DisplayName("Should throw exception when order items is empty")
        void testCreateOrder_EmptyItems_ThrowsException() {
            // Arrange
            OrderRequest emptyRequest = OrderRequest.builder()
                    .userId(testUserId)
                    .orderItems(new ArrayList<>())
                    .build();

            // Act & Assert
            assertThrows(BusinessLogicException.class, () -> {
                orderService.createOrder(emptyRequest);
            });

            verify(orderRepository, never()).save(any());
            log.info("Validation: Empty order items correctly rejected");
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void testCreateOrder_ProductNotFound_ThrowsException() {
            // Arrange: Create request with non-existent product
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId("PROD-999")
                    .quantity(1)
                    .price(100000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .userId(testUserId)
                    .orderItems(Arrays.asList(itemRequest))
                    .build();

            when(productService.getProductById("PROD-999"))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                orderService.createOrder(request);
            });

            verify(orderRepository, never()).save(any());
            log.info("Validation: Non-existent product correctly rejected");
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void testCreateOrder_InsufficientStock_ThrowsException() {
            // Arrange
            when(productService.getProductById("PROD-001")).thenReturn(testProduct);
            when(inventoryService.hasEnoughStock("PROD-001", 1)).thenReturn(false);

            // Act & Assert
            assertThrows(BusinessLogicException.class, () -> {
                orderService.createOrder(testOrderRequest);
            });

            verify(orderRepository, never()).save(any());
            log.info("Validation: Insufficient stock correctly rejected");
        }
    }

    @Nested
    @DisplayName("Order Retrieval Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should retrieve order by ID")
        void testGetOrderById_Success() {
            // Arrange
            String orderId = "ORD-001";
            Order order = Order.builder()
                    .id(orderId)
                    .userId(testUserId)
                    .totalPrice(129900L)
                    .shippingFee(29900L)
                    .status(OrderStatus.PENDING)
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
            when(orderMapper.toOrderResponse(order)).thenReturn(testOrderResponse);

            // Act
            OrderResponse result = orderService.getOrderById(orderId);

            // Assert
            assertNotNull(result);
            assertEquals(testUserId, result.getUserId());
            assertEquals(29900L, result.getShippingFee());

            verify(orderRepository).findById(orderId);
            verify(orderMapper).toOrderResponse(order);

            log.info("Order retrieved successfully: {}", orderId);
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void testGetOrderById_NotFound_ThrowsException() {
            // Arrange
            when(orderRepository.findById("ORD-999"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> {
                orderService.getOrderById("ORD-999");
            });

            verify(orderRepository).findById("ORD-999");
            log.info("Validation: Non-existent order correctly rejected");
        }
    }

    @Nested
    @DisplayName("Shipping Fee Tests")
    class ShippingFeeTests {

        @Test
        @DisplayName("Shipping fee should always be 29,900 VND")
        void testShippingFee_AlwaysFixed() {
            // Arrange: Multiple orders with different amounts
            List<Long> subtotals = Arrays.asList(1000L, 50000L, 100000L, 1000000L);

            for (Long subtotal : subtotals) {
                when(productService.getProductById(anyString())).thenReturn(testProduct);
                when(inventoryService.hasEnoughStock(anyString(), anyInt())).thenReturn(true);

                OrderItemRequest itemRequest = OrderItemRequest.builder()
                        .productId("PROD-001")
                        .quantity(1)
                        .price(subtotal)
                        .build();

                OrderRequest request = OrderRequest.builder()
                        .userId(testUserId)
                        .orderItems(Arrays.asList(itemRequest))
                        .build();

                ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
                when(orderRepository.save(orderCaptor.capture())).thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    return order;
                });
                when(orderMapper.toOrderResponse(any(Order.class))).thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    return OrderResponse.builder()
                            .shippingFee(order.getShippingFee())
                            .totalPrice(order.getTotalPrice())
                            .build();
                });

                // Act
                OrderResponse result = orderService.createOrder(request);

                // Assert
                assertEquals(29900L, result.getShippingFee());
                assertEquals(subtotal + 29900L, result.getTotalPrice());

                log.info("Verified shipping fee (29,900 VND) for subtotal: {} VND", subtotal);
            }
        }
    }
}
