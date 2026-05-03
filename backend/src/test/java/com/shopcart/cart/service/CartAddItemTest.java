package com.shopcart.cart.service;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("Cart Service — Add to cart")
class CartAddItemTest extends BaseCartServiceTest {

    @DisplayName("TC1: Thêm sản phẩm mới vào giỏ hàng thành công")
    @Test
    void addToCart_NewProduct_Success() {
        // 1. Arrange
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());

        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                .thenReturn(Optional.empty());

        when(inventoryService.hasEnoughStock(request.getProductId(), request.getQuantity()))
                .thenReturn(true);

        mockBuildCartResponseHelper(List.of(new CartItem()));

        // 2. Act
        CartResponse response = cartService.addToCart(userId, request);

        // 3. Assert
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
        // 1. Arrange
        int existingQuantity = 3;
        int expectedNewQuantity = request.getQuantity() + existingQuantity;
        // Giả lập sản phẩm đã có trong giỏ
        CartItem existingCartItem = CartItem.builder()
                .userId(userId)
                .productId(request.getProductId())
                .quantity(existingQuantity)
                .build();

        // Giả lập sản phẩm hợp lệ
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());

        // Giả lập repository trả về sản phẩm ĐÃ TỒN TẠI
        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                .thenReturn(Optional.of(existingCartItem));

        // Mock kiểm tra tồn kho phải dùng TỔNG SỐ LƯỢNG MỚI (expectedNewQuantity)
        when(inventoryService.hasEnoughStock(request.getProductId(), expectedNewQuantity))
                .thenReturn(true);

        mockBuildCartResponseHelper(List.of(new CartItem()));

        // 2. Act
        CartResponse response = cartService.addToCart(userId, request);

        // 3. Assert

        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);

        verify(cartRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();

        assertEquals(userId, savedCartItem.getUserId());
        assertEquals(request.getProductId(), savedCartItem.getProductId());
        assertEquals(expectedNewQuantity, savedCartItem.getQuantity());

        assertNotNull(response);
    }

    @DisplayName("TC3: Thêm sản phẩm với số lượng tồn kho không đủ")
    @Test
    void addToCart_InsufficientStock_ThrowsException() {
        // 1. Arrange
        // Giả lập sản phẩm tồn tại
        when(productService.getProductById(request.getProductId())).thenReturn(new Product());

        // Giả lập sản phẩm chưa có trong giỏ hàng
        when(cartRepository.findByUserIdAndProductId(userId, request.getProductId()))
                .thenReturn(Optional.empty());

        // Giả lập kho KHÔNG đủ số lượng (trả về false)
        when(inventoryService.hasEnoughStock(request.getProductId(), request.getQuantity()))
                .thenReturn(false);

        // 2. Act
        // Kiểm tra xem Service có ném đúng BusinessLogicException hay không
        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            cartService.addToCart(userId, request);
        });

        // 3. Assert
        // Kiểm tra nội dung message trong exception có khớp không
        String expectedMessage = "Insufficient stock for product: " + request.getProductId();
        assertEquals(expectedMessage, exception.getMessage());

        // Đảm bảo hàm save() KHÔNG bao giờ được gọi khi có lỗi
        verify(cartRepository, never()).save(any(CartItem.class));

        // Đảm bảo mapper cũng không được gọi
        verify(cartMapper, never()).toCartResponse(anyString(), anyList());
    }

    @DisplayName("TC4: Thêm sản phẩm không tồn tại vào giỏ")
    @Test
    void addToCart_ProductNotFound_ThrowsException() {
        // 1. Arrange
        // Giả lập ProductService ném ra exception khi tìm kiếm product id này
        when(productService.getProductById(request.getProductId()))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        // 2. Act & Assert
        // Kiểm chứng xem hàm addToCart có quăng đúng lỗi ResourceNotFoundException ra không
        assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart(userId, request);
        });

        // 3. Verify
        // Đảm bảo rằng luồng code đã dừng lại ngay lập tức và KHÔNG CÓ hàm nào bên dưới được gọi
        verify(cartRepository, never()).findByUserIdAndProductId(anyString(), anyString());
        verify(inventoryService, never()).hasEnoughStock(anyString(), anyInt());
        verify(cartRepository, never()).save(any(CartItem.class));
    }
}
