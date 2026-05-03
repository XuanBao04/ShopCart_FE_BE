package com.shopcart.cart.service;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Cart Service — Update quantity")
class CartUpdateItemTest extends BaseCartServiceTest {

    private Long cartItemId;
    private Integer newQuantity;

    @BeforeEach
    void setUpUpdate() {
        cartItemId = 999L;
        newQuantity = 10;
    }

    @Test
    @DisplayName("TC1: Cập nhật số lượng thành công")
    void updateQuantity_Success() {
        // 1. Arrange
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));

        when(inventoryService.hasEnoughStock(validCartItem.getProductId(), newQuantity))
                .thenReturn(true);

        mockBuildCartResponseHelper(List.of(validCartItem));

        // 2. Act
        CartResponse response = cartService.updateQuantity(userId, cartItemId, newQuantity);

        // 3. Assert
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());

        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(newQuantity, savedCartItem.getQuantity());
        assertNotNull(response);
    }

    @DisplayName("TC2: Cập nhật thất bại do kho không đủ hàng")
    @Test
    void updateQuantity_InsufficientStock_ThrowsException() {
        // 1. Arrange
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));

        when(inventoryService.hasEnoughStock(validCartItem.getProductId(), newQuantity))
                .thenReturn(false);

        // 2. Act & Assert
        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            cartService.updateQuantity(userId, cartItemId, newQuantity);
        });

        assertEquals("Insufficient stock for product: " + validCartItem.getProductId(), exception.getMessage());

        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @DisplayName("TC3: Cập nhật thất bại do Cart Item không tồn tại")
    @Test
    void updateQuantity_ItemNotFound_ThrowsException() {
        // 1. Arrange
        when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateQuantity(userId, cartItemId, newQuantity);
        });

        assertEquals("Không tìm thấy cart item với id: " + cartItemId, exception.getMessage());

        verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @DisplayName("TC4: Cập nhật thất bại do Item thuộc về User khác (Bảo mật IDOR)")
    @Test
    void updateQuantity_WrongUser_ThrowsException() {
        // 1. Arrange
        String hackerId = "hacker-999";
        CartItem someoneElsesItem = CartItem.builder()
                .userId(hackerId)
                .productId("PROD-001")
                .quantity(1)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(someoneElsesItem));

        // 2. Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateQuantity(userId, cartItemId, newQuantity);
        });

        assertEquals("Cart item không thuộc về user: " + userId, exception.getMessage());

        verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
        verify(cartRepository, never()).save(any(CartItem.class));
    }
}
