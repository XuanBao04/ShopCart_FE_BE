package com.shopcart.cart.service.clear;

import com.shopcart.cart.service.BaseCartServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("Cart Service — Làm trống giỏ hàng (Luồng thành công)")
class CartClearHappyPathTest extends BaseCartServiceTest {

    @DisplayName("TC1: Xóa toàn bộ giỏ hàng của người dùng thành công")
    @Test
    void clearCart_Success() {
        cartService.clearCart(userId);
        verify(cartRepository, times(1)).deleteByUserId(userId);
    }
}
