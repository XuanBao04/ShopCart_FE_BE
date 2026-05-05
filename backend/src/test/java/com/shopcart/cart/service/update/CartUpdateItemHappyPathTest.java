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
    @DisplayName("TC1: Cập nhật số lượng thành công")
    void updateQuantity_Success() {
        Long cartItemId = 999L;
        Integer newQuantity = 10;
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
        when(inventoryService.hasEnoughStock(validCartItem.getProductId(), newQuantity))
                .thenReturn(true);
        mockBuildCartResponseHelper(List.of(validCartItem));

        CartResponse response = cartService.updateQuantity(userId, cartItemId, newQuantity);

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(newQuantity, savedCartItem.getQuantity());
        assertNotNull(response);
    }
}
