package com.shopcart.cart.service;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Cart Service — Remove from cart")
class CartRemoveItemTest extends BaseCartServiceTest {

    private Long cartItemId;

    @BeforeEach
    void setUpRemove() {
        cartItemId = 999L;
    }

    @Test
    @DisplayName("TC1: Xóa cart item thành công")
    void removeFromCart_Success() {
        // 1. Arrange
        CartItem validCartItem = CartItem.builder()
                .userId(userId) // Trùng khớp với userId đang request
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));

        mockBuildCartResponseHelper(List.of()); // Truyền list rỗng vì item đã bị xóa

        // 2. Act
        CartResponse response = cartService.removeFromCart(userId, cartItemId);

        // 3. Assert
        // Đảm bảo hàm delete(cartItem) được gọi ĐÚNG 1 LẦN với đúng đối tượng đó
        verify(cartRepository, times(1)).delete(validCartItem);
        assertNotNull(response);
    }

    @Test
    @DisplayName("TC2: Xóa thất bại do không tìm thấy ID trong DB")
    void removeFromCart_ItemNotFound_ThrowsException() {
        // 1. Arrange
        when(cartRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // 2. Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(userId, cartItemId);
        });

        assertEquals("Không tìm thấy cart item với id: " + cartItemId, exception.getMessage());

        // 3. Verify: Tuyệt đối hàm delete KHÔNG bao giờ được gọi
        verify(cartRepository, never()).delete(any(CartItem.class));
    }

    @Test
    @DisplayName("TC3: Xóa thất bại do Item thuộc về User khác (Bảo mật IDOR)")
    void removeFromCart_WrongUser_ThrowsException() {
        // 1. Arrange
        String hackerId = "hacker-999";

        // Giả lập item có tồn tại, nhưng userId lại là của người khác
        CartItem someoneElsesItem = CartItem.builder()
                .userId(hackerId)
                .productId("PROD-001")
                .quantity(1)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(someoneElsesItem));

        // 2. Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(userId, cartItemId);
        });

        assertEquals("Cart item không thuộc về user: " + userId, exception.getMessage());

        // 3. Verify : Không bao giờ được phép xóa
        verify(cartRepository, never()).delete(any(CartItem.class));
    }
}
