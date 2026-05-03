package com.shopcart.cart.service;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Cart Service — Get cart")
class CartGetCartTest extends BaseCartServiceTest {

    @Test
    @DisplayName("TC1: Lấy giỏ hàng thành công khi có sản phẩm")
    void getCart_WithItems_Success() {
        // 1. Arrange
        CartItem item1 = CartItem.builder().userId(userId).productId("PROD-001").quantity(1).build();
        CartItem item2 = CartItem.builder().userId(userId).productId("PROD-002").quantity(3).build();
        List<CartItem> mockCartItems = List.of(item1, item2);

        // Giả lập DB trả về danh sách 2 sản phẩm này
        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(mockCartItems);

        // Giả lập Mapper biến danh sách thành CartResponse
        CartResponse mockResponse = new CartResponse();
        when(cartMapper.toCartResponse(userId, mockCartItems))
                .thenReturn(mockResponse);

        // 2. Act
        CartResponse actualResponse = cartService.getCart(userId);

        // 3. Assert
        assertNotNull(actualResponse);
        assertEquals(mockResponse, actualResponse); // Đảm bảo trả về đúng cái response mà mapper đã build

        // Đảm bảo các dependency được gọi đúng 1 lần
        verify(cartRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
        verify(cartMapper, times(1)).toCartResponse(userId, mockCartItems);
    }

    @Test
    @DisplayName("TC2: Lấy giỏ hàng thành công khi giỏ hàng trống")
    void getCart_EmptyCart_Success() {
        // 1. Arrange
        // Giỏ hàng trống -> DB trả về list rỗng
        List<CartItem> emptyCartItems = List.of();

        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(emptyCartItems);

        CartResponse mockEmptyResponse = new CartResponse();
        when(cartMapper.toCartResponse(userId, emptyCartItems))
                .thenReturn(mockEmptyResponse);

        // 2. Act
        CartResponse actualResponse = cartService.getCart(userId);

        // 3. Assert
        assertNotNull(actualResponse);
        assertEquals(mockEmptyResponse, actualResponse);

        verify(cartRepository, times(1)).findByUserIdOrderByCreatedAtDesc(userId);
        verify(cartMapper, times(1)).toCartResponse(userId, emptyCartItems);
    }
}
