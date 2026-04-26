package com.shopcart.service;

import com.shopcart.dto.request.CartItemRequest;
import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.entity.enums.ProductStatus;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.mapper.CartMapper;
import com.shopcart.repository.CartRepository;
import com.shopcart.service.impl.CartServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for CartService using Lombok + JUnit 5 + Mockito
 * 
 * Lombok Benefits:
 * - @Slf4j: Automatic logger creation
 * - @Builder: Easy test data creation with CartItem.builder()
 * - @Data: Automatic getters/setters for assertions
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private IProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    private String testUserId;
    private CartItem testCartItem;
    private CartItemRequest testCartItemRequest;
    private CartResponse testCartResponse;

    /**
     * Setup test data using Lombok @Builder - Very clean and readable!
     */
    @BeforeEach
    void setUp() {
        testUserId = "user-123";
        
        // Using Lombok @Builder for clean test data creation
        testCartItem = CartItem.builder()
                .id(1L)
                .userId(testUserId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        testCartItemRequest = CartItemRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .build();

        CartItemResponse cartItemResponse = CartItemResponse.builder()
                .id(1L)
                .productId("PROD-001")
                .quantity(2)
                .build();

        testCartResponse = CartResponse.builder()
                .userId(testUserId)
                .items(Arrays.asList(cartItemResponse))
                .totalItems(2)
                .totalPrice(50000L)
                .build();

        log.info("Test setup completed with userId: {}", testUserId);
    }

    @Test
    @DisplayName("Should get cart successfully")
    void testGetCart_Success() {
        // Arrange
        List<CartItem> cartItems = Arrays.asList(testCartItem);
        when(cartRepository.findByUserId(testUserId)).thenReturn(cartItems);
        when(cartMapper.toCartResponse(anyString(), any())).thenReturn(testCartResponse);

        // Act
        CartResponse result = cartService.getCart(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals(50000L, result.getTotalPrice());

        // Verify mocks were called
        verify(cartRepository, times(1)).findByUserId(testUserId);
        verify(cartMapper, times(1)).toCartResponse(testUserId, cartItems);
    }

    @Test
    @DisplayName("Should add new item to cart")
    void testAddToCart_NewItem_Success() {
        // Arrange
        when(productService.getProductById(testCartItemRequest.getProductId())).thenReturn(
                Product.builder()
                        .id("PROD-001")
                        .name("Test Product")
                        .price(25000L)
                        .status(ProductStatus.ACTIVE)
                        .build()
        );
        when(cartRepository.findByUserIdAndProductId(testUserId, testCartItemRequest.getProductId()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(testCartItem));
        when(cartMapper.toCartResponse(anyString(), any())).thenReturn(testCartResponse);

        // Act
        CartResponse result = cartService.addToCart(testUserId, testCartItemRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        
        // Verify product validation was called
        verify(productService).getProductById(testCartItemRequest.getProductId());
        // Verify save was called
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should update existing cart item quantity")
    void testAddToCart_UpdateExisting_Success() {
        // Arrange
        CartItem existingCartItem = CartItem.builder()
                .id(1L)
                .userId(testUserId)
                .productId("PROD-001")
                .quantity(1)  // existing quantity
                .build();

        when(productService.getProductById(testCartItemRequest.getProductId())).thenReturn(
                Product.builder().id("PROD-001").build()
        );
        when(cartRepository.findByUserIdAndProductId(testUserId, testCartItemRequest.getProductId()))
                .thenReturn(Optional.of(existingCartItem));
        when(cartRepository.save(any(CartItem.class))).thenReturn(existingCartItem);
        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(existingCartItem));
        when(cartMapper.toCartResponse(anyString(), any())).thenReturn(testCartResponse);

        // Act
        CartResponse result = cartService.addToCart(testUserId, testCartItemRequest);

        // Assert - quantity should be incremented (1 + 2 = 3)
        assertNotNull(result);
        verify(cartRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Should remove cart item successfully")
    void testRemoveFromCart_Success() {
        // Arrange
        long cartItemId = 1L;
        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(testCartItem));
        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList());
        when(cartMapper.toCartResponse(anyString(), any())).thenReturn(
                CartResponse.builder()
                        .userId(testUserId)
                        .items(Arrays.asList())
                        .totalItems(0)
                        .totalPrice(0L)
                        .build()
        );

        // Act
        CartResponse result = cartService.removeFromCart(testUserId, cartItemId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        verify(cartRepository).deleteById(cartItemId);
    }

    @Test
    @DisplayName("Should throw exception when cart item not found")
    void testRemoveFromCart_ItemNotFound() {
        // Arrange
        long cartItemId = 999L;
        when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(testUserId, cartItemId);
        });

        verify(cartRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw exception when cart item belongs to different user")
    void testRemoveFromCart_WrongUser() {
        // Arrange
        long cartItemId = 1L;
        CartItem otherUserCartItem = CartItem.builder()
                .id(cartItemId)
                .userId("other-user")
                .productId("PROD-001")
                .quantity(1)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(otherUserCartItem));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(testUserId, cartItemId);
        });

        verify(cartRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should update cart item quantity successfully")
    void testUpdateCartItem_Success() {
        // Arrange
        long cartItemId = 1L;
        Integer newQuantity = 5;
        
        CartItem itemToUpdate = CartItem.builder()
                .id(cartItemId)
                .userId(testUserId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(itemToUpdate));
        when(cartRepository.save(any(CartItem.class))).thenReturn(itemToUpdate);
        when(cartRepository.findByUserId(testUserId)).thenReturn(Arrays.asList(itemToUpdate));
        when(cartMapper.toCartResponse(anyString(), any())).thenReturn(testCartResponse);

        // Act
        CartResponse result = cartService.updateQuantity(testUserId, cartItemId, newQuantity);

        // Assert
        assertNotNull(result);
        verify(cartRepository).save(any(CartItem.class));
    }
}
