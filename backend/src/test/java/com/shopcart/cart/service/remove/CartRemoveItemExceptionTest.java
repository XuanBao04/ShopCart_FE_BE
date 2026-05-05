package com.shopcart.cart.service.remove;

import com.shopcart.constant.MessageConstant;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Xóa sản phẩm khỏi giỏ hàng (Trường hợp lỗi)")
class CartRemoveItemExceptionTest extends BaseCartServiceTest {

    @Test
    @DisplayName("TC2: Xóa thất bại do không tìm thấy ID trong DB")
    void removeFromCart_ItemNotFound_ThrowsException() {
        Long cartItemId = 999L;
        when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(userId, cartItemId);
        });

        assertEquals(MessageConstant.Cart.NOT_FOUND + cartItemId, exception.getMessage());
        verify(cartRepository, never()).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("TC3: Xóa thất bại do Item thuộc về User khác (Bảo mật IDOR)")
    void removeFromCart_WrongUser_ThrowsException() {
        Long cartItemId = 999L;
        String hackerId = "hacker-999";
        CartItem someoneElsesItem = CartItem.builder()
                .userId(hackerId)
                .productId("PROD-001")
                .quantity(1)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(someoneElsesItem));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(userId, cartItemId);
        });

        assertEquals(MessageConstant.Cart.WRONG_USER + userId, exception.getMessage());
        verify(cartRepository, never()).delete(any(CartItem.class));
    }
}
