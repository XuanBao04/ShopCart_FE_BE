package com.shopcart.cart.service.get;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Lấy thông tin giỏ hàng (Luồng thành công)")
class CartGetCartHappyPathTest extends BaseCartServiceTest {

    @DisplayName("TC1: Lấy thông tin giỏ hàng của người dùng thành công")
    @Test
    void getCart_Success() {
        CartItem item1 = CartItem.builder().id(1L).productId("prod-1").quantity(2).build();
        CartItem item2 = CartItem.builder().id(2L).productId("prod-2").quantity(1).build();
        List<CartItem> mockItems = List.of(item1, item2);
        
        Map<String, Product> productsById = new HashMap<>();
        productsById.put("prod-1", Product.builder().id("prod-1").price(100_000L).build());
        productsById.put("prod-2", Product.builder().id("prod-2").price(50_000L).build());
        
        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(mockItems);
        when(productRepository.findAllById(any())).thenReturn(productsById.values().stream().toList());
        when(cartMapper.toCartResponse(userId, mockItems, productsById)).thenReturn(new CartResponse());

        CartResponse response = cartService.getCart(userId);

        assertNotNull(response);
    }
}
