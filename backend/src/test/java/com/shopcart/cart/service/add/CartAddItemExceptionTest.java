package com.shopcart.cart.service.add;

import com.shopcart.cart.service.BaseCartServiceTest;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.product.entity.Product;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cart Service — Thêm vào giỏ hàng (Trường hợp lỗi)")
class CartAddItemExceptionTest extends BaseCartServiceTest {

    @DisplayName("TC3: Thêm sản phẩm với số lượng tồn kho không đủ")
    @Test
    void addToCart_InsufficientStock_ThrowsException() {
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());
        
        // Mock reserveStock to throw exception since it's now used instead of hasEnoughStock
        doThrow(new BusinessLogicException(com.shopcart.constant.MessageConstant.Inventory.INSUFFICIENT_STOCK_RESERVE + request.getProductId()))
                .when(inventoryService).reserveStock(request.getProductId(), request.getQuantity());

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            cartService.addToCart(userId, request);
        });
        
        String expectedMessage = com.shopcart.constant.MessageConstant.Inventory.INSUFFICIENT_STOCK_RESERVE + request.getProductId();
        assertEquals(expectedMessage, exception.getMessage());
        verify(cartRepository, never()).save(any(CartItem.class));
        verify(cartMapper, never()).toCartResponse(anyString(), anyList(), any());
    }

    @DisplayName("TC4: Thêm sản phẩm không tồn tại vào giỏ")
    @Test
    void addToCart_ProductNotFound_ThrowsException() {
        when(productService.getProductById(request.getProductId()))
                .thenThrow(new ResourceNotFoundException("Product not found"));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart(userId, request);
        });
        
        verify(cartRepository, never()).findByUserIdAndProductId(anyString(), anyString());
        verify(inventoryService, never()).reserveStock(anyString(), anyInt());
        verify(cartRepository, never()).save(any(CartItem.class));
    }
}
