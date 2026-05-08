package com.shopcart.cart.service.clear;

import com.shopcart.cart.service.BaseCartServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Làm trống giỏ hàng (Luồng thành công)")
class CartClearHappyPathTest extends BaseCartServiceTest {

    @DisplayName("TC1: Xóa toàn bộ giỏ hàng của người dùng thành công")
    @Test
    void clearCart_Success() {
        com.shopcart.cart.entity.CartItem item1 = com.shopcart.cart.entity.CartItem.builder()
                .productId("P1").quantity(2).build();
        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(java.util.List.of(item1));

        cartService.clearCart(userId);
        
        verify(inventoryService, times(1)).releaseStock("P1", 2);
        verify(cartRepository, times(1)).deleteByUserId(userId);
    }
}
