package com.shopcart.product.service.search;

import com.shopcart.product.entity.Product;
import com.shopcart.product.factory.ProductTestFactory;
import com.shopcart.product.service.BaseProductServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("Product Service — Tìm kiếm sản phẩm (Luồng thành công)")
class ProductSearchHappyPathTest extends BaseProductServiceTest {

    @Test
    @DisplayName("Nên tìm kiếm sản phẩm theo tên thành công")
    void testSearchProductsByName_Success() {
        List<Product> allProducts = Arrays.asList(
                ProductTestFactory.activeProduct("P1", "Laptop Gaming"),
                ProductTestFactory.activeProduct("P2", "Office Laptop"),
                ProductTestFactory.activeProduct("P3", "Mouse")
        );
        when(productRepository.findAll()).thenReturn(allProducts);

        List<Product> results = productService.searchProductsByName("Laptop");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getName().contains("Laptop")));
    }

    @Test
    @DisplayName("Nên thực hiện tìm kiếm không phân biệt hoa thường")
    void testSearchProductsByName_CaseInsensitive() {
        List<Product> allProducts = Arrays.asList(
                ProductTestFactory.activeProduct("P1", "iPhone 15"),
                ProductTestFactory.activeProduct("P2", "Samsung S23")
        );
        when(productRepository.findAll()).thenReturn(allProducts);

        List<Product> results = productService.searchProductsByName("IPHONE");

        assertEquals(1, results.size());
        assertEquals("iPhone 15", results.get(0).getName());
    }
}
