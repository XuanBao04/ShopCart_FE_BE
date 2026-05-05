package com.shopcart.product.service.get;

import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.product.service.BaseProductServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Product Service — Lấy thông tin sản phẩm (Trường hợp lỗi)")
class ProductGetExceptionTest extends BaseProductServiceTest {

    @Test
    @DisplayName("Nên ném lỗi ResourceNotFoundException khi không tìm thấy sản phẩm")
    void testGetProductById_NotFound() {
        when(productRepository.findById("INVALID-ID")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById("INVALID-ID")
        );

        assertEquals("Product not found with id: INVALID-ID", exception.getMessage());
    }
}
