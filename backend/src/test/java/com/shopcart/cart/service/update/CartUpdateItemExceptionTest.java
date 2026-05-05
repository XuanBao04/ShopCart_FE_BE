package com.shopcart.cart.service.update;

import com.shopcart.constant.MessageConstant;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Cập nhật số lượng (Trường hợp lỗi)")
class CartUpdateItemExceptionTest extends BaseCartServiceTest {

    @DisplayName("TC2: Cập nhật thất bại do kho không đủ hàng")
    @Test
    void updateQuantity_InsufficientStock_ThrowsException() {
        Long cartItemId = 999L;
        Integer newQuantity = 10;
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
        when(inventoryService.hasEnoughStock(validCartItem.getProductId(), newQuantity))
                .thenReturn(false);

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            cartService.updateQuantity(userId, cartItemId, newQuantity);
        });

        assertEquals(MessageConstant.Inventory.INSUFFICIENT_STOCK + validCartItem.getProductId(), exception.getMessage());
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @DisplayName("TC3: Cập nhật thất bại do Cart Item không tồn tại")
    @Test
    void updateQuantity_ItemNotFound_ThrowsException() {
        Long cartItemId = 999L;
        Integer newQuantity = 10;
        when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateQuantity(userId, cartItemId, newQuantity);
        });

        assertEquals(MessageConstant.Cart.NOT_FOUND + cartItemId, exception.getMessage());
        verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @DisplayName("TC4: Cập nhật thất bại do Item thuộc về User khác (Bảo mật IDOR)")
    @Test
    void updateQuantity_WrongUser_ThrowsException() {
        Long cartItemId = 999L;
        Integer newQuantity = 10;
        String hackerId = "hacker-999";
        CartItem someoneElsesItem = CartItem.builder()
                .userId(hackerId)
                .productId("PROD-001")
                .quantity(1)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(someoneElsesItem));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateQuantity(userId, cartItemId, newQuantity);
        });

        assertEquals(MessageConstant.Cart.WRONG_USER + userId, exception.getMessage());
        verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
        verify(cartRepository, never()).save(any(CartItem.class));
    }
}
