package com.shopcart.cart.service.remove;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Xóa sản phẩm khỏi giỏ hàng (Luồng thành công)")
class CartRemoveItemHappyPathTest extends BaseCartServiceTest {

    @Test
    @DisplayName("TC1: Xóa cart item thành công")
    void removeFromCart_Success() {
        Long cartItemId = 999L;
        CartItem validCartItem = CartItem.builder()
                .userId(userId)
                .productId("PROD-001")
                .quantity(2)
                .build();

        when(cartRepository.findById(cartItemId)).thenReturn(Optional.of(validCartItem));
        mockBuildCartResponseHelper(List.of());

        CartResponse response = cartService.removeFromCart(userId, cartItemId);

        // Verify releaseStock was called
        verify(inventoryService, times(1)).releaseStock(validCartItem.getProductId(), validCartItem.getQuantity());
        
        verify(cartRepository, times(1)).delete(validCartItem);
        assertNotNull(response);
    }
}
