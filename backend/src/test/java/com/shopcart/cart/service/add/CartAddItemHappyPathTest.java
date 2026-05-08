package com.shopcart.cart.service.add;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.product.entity.Product;
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

@DisplayName("Cart Service — Thêm vào giỏ hàng (Luồng thành công)")
class CartAddItemHappyPathTest extends BaseCartServiceTest {

    @DisplayName("TC1: Thêm sản phẩm mới vào giỏ hàng thành công")
    @Test
    void addToCart_NewProduct_Success() {
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());
        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                .thenReturn(Optional.empty());
        
        mockBuildCartResponseHelper(List.of(new CartItem()));
        
        CartResponse response = cartService.addToCart(userId, request);
        
        // Verify reserveStock was called
        verify(inventoryService, times(1)).reserveStock(request.getProductId(), request.getQuantity());
        
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(userId, savedCartItem.getUserId());
        assertEquals(request.getProductId(), savedCartItem.getProductId());
        assertEquals(request.getQuantity(), savedCartItem.getQuantity());
        assertNotNull(response);
    }

    @DisplayName("TC2: Thêm sản phẩm đã có trong giỏ")
    @Test
    void addToCart_ExistingProduct_Success() {
        int existingQuantity = 3;
        int expectedNewQuantity = request.getQuantity() + existingQuantity;
        CartItem existingCartItem = CartItem.builder()
                .userId(userId)
                .productId(request.getProductId())
                .quantity(existingQuantity)
                .build();
                
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());
        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                .thenReturn(Optional.of(existingCartItem));
                
        mockBuildCartResponseHelper(List.of(new CartItem()));
        
        CartResponse response = cartService.addToCart(userId, request);
        
        // Verify reserveStock was called with ONLY the new quantity
        verify(inventoryService, times(1)).reserveStock(request.getProductId(), request.getQuantity());
        
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertEquals(userId, savedCartItem.getUserId());
        assertEquals(request.getProductId(), savedCartItem.getProductId());
        assertEquals(expectedNewQuantity, savedCartItem.getQuantity());
        assertNotNull(response);
    }
}
