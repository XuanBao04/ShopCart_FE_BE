package com.shopcart.cart.service.get;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Lấy thông tin giỏ hàng (Luồng thành công)")
class CartGetCartHappyPathTest extends BaseCartServiceTest {

    @DisplayName("TC1: Lấy thông tin giỏ hàng của người dùng thành công")
    @Test
    void getCart_Success() {
        List<CartItem> mockItems = List.of(new CartItem(), new CartItem());
        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(mockItems);
        when(cartMapper.toCartResponse(userId, mockItems)).thenReturn(new CartResponse());

        CartResponse response = cartService.getCart(userId);

        assertNotNull(response);
    }
}
