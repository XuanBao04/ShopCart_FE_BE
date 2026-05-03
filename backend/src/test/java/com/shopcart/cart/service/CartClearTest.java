package com.shopcart.cart.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("Cart Service — Clear cart")
class CartClearTest extends BaseCartServiceTest {

    @Test
    @DisplayName("TC1: Xóa toàn bộ giỏ hàng thành công")
    void clearCart_Success() {
        // Đối với hàm void như deleteByUserId, Mockito mặc định không làm gì cả.
        // 1. Act
        cartService.clearCart(userId);
        // 2. Assert
        verify(cartRepository, times(1)).deleteByUserId(userId);
    }
}
