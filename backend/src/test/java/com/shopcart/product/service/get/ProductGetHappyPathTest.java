package com.shopcart.product.service.get;

import com.shopcart.product.entity.Product;
import com.shopcart.product.factory.ProductTestFactory;
import com.shopcart.product.service.BaseProductServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@DisplayName("Product Service — Lấy thông tin sản phẩm (Luồng thành công)")
class ProductGetHappyPathTest extends BaseProductServiceTest {

    @Test
    @DisplayName("Nên lấy được tất cả sản phẩm thành công")
    void testGetAllProducts_Success() {
        List<Product> products = Arrays.asList(
                ProductTestFactory.activeProduct("PROD-1", "Product 1"),
                ProductTestFactory.activeProduct("PROD-2", "Product 2")
        );
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Nên lấy được sản phẩm theo ID thành công")
    void testGetProductById_Success() {
        Product product = ProductTestFactory.activeProduct("PROD-1", "Product 1");
        when(productRepository.findById("PROD-1")).thenReturn(Optional.of(product));

        Product result = productService.getProductById("PROD-1");

        assertNotNull(result);
        assertEquals("PROD-1", result.getId());
        assertEquals("Product 1", result.getName());
    }
}
