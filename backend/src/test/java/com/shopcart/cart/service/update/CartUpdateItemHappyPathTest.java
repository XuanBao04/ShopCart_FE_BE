package com.shopcart.cart.service.update;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Cập nhật số lượng (Luồng thành công)")
class CartUpdateItemHappyPathTest extends BaseCartServiceTest {

    @Test
    @DisplayName("TC1: Cập nhật số lượng thành công (Tăng số lượng)")
    void updateQuantity_Increase_Success() {
        Long cartItemId = 999L;
        Integer newQuantity = 10;
        int oldQuantity = 2;
        int diff = newQuantity - oldQuantity;
        
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(oldQuantity)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
        mockBuildCartResponseHelper(List.of(validCartItem));

        CartResponse response = cartService.updateQuantity(userId, cartItemId, newQuantity);

        // Verify reserveStock was called with the diff
        verify(inventoryService, times(1)).reserveStock(validCartItem.getProductId(), diff);
        
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(newQuantity, savedCartItem.getQuantity());
        assertNotNull(response);
    }

    @Test
    @DisplayName("TC2: Cập nhật số lượng thành công (Giảm số lượng)")
    void updateQuantity_Decrease_Success() {
        Long cartItemId = 999L;
        Integer newQuantity = 1;
        int oldQuantity = 5;
        int diff = oldQuantity - newQuantity; // release 4
        
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(oldQuantity)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
        mockBuildCartResponseHelper(List.of(validCartItem));

        CartResponse response = cartService.updateQuantity(userId, cartItemId, newQuantity);

        // Verify releaseStock was called with the diff
        verify(inventoryService, times(1)).releaseStock(validCartItem.getProductId(), diff);
        
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(newQuantity, savedCartItem.getQuantity());
        assertNotNull(response);
    }
}
