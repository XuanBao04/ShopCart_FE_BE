package com.shopcart.product.service.inventory;

import com.shopcart.product.entity.Product;
import com.shopcart.product.factory.ProductTestFactory;
import com.shopcart.product.service.BaseProductServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("Product Service — Tích hợp kho hàng (Luồng thành công)")
class ProductInventoryHappyPathTest extends BaseProductServiceTest {

    @Test
    @DisplayName("Nên lấy được số lượng tồn kho khả dụng của sản phẩm")
    void testGetAvailableStock_Success() {
        String productId = "PROD-1";
        Product product = ProductTestFactory.activeProduct(productId, "Product 1");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryService.getStock(productId)).thenReturn(50);

        Integer result = productService.getAvailableStock(productId);

        assertEquals(50, result);
    }

    @Test
    @DisplayName("Nên xác nhận sản phẩm còn hàng thành công")
    void testIsProductAvailable_True() {
        String productId = "PROD-1";
        when(inventoryService.hasEnoughStock(productId, 1)).thenReturn(true);

        boolean result = productService.isProductAvailable(productId);

        assertTrue(result);
    }

    @Test
    @DisplayName("Nên trả về false khi sản phẩm hết hàng")
    void testIsProductAvailable_False() {
        String productId = "PROD-1";
        when(inventoryService.hasEnoughStock(productId, 1)).thenReturn(false);

        boolean result = productService.isProductAvailable(productId);

        assertFalse(result);
    }
}
