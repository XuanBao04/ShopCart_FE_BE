package com.shopcart.product.controller;

import com.shopcart.product.entity.Product;
import com.shopcart.common.enums.ProductStatus;
import com.shopcart.product.service.IProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller test
@DisplayName("Product Controller — Kiểm thử lớp Controller")
class ProductControllerLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProductService productService;

    @Test
    @DisplayName("Nên lấy được tất cả sản phẩm")
    void getAllProducts_Success() throws Exception {
        Product p1 = Product.builder().id("P1").name("Product 1").price(100L).status(ProductStatus.ACTIVE).build();
        when(productService.getAllProducts()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("P1"))
                .andExpect(jsonPath("$[0].name").value("Product 1"));
    }

    @Test
    @DisplayName("Nên lấy được sản phẩm theo ID")
    void getProductById_Success() throws Exception {
        Product p1 = Product.builder().id("P1").name("Product 1").price(100L).status(ProductStatus.ACTIVE).build();
        when(productService.getProductById("P1")).thenReturn(p1);

        mockMvc.perform(get("/api/products/P1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("P1"))
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    @DisplayName("Nên tìm kiếm được sản phẩm theo từ khóa")
    void searchProducts_Success() throws Exception {
        Product p1 = Product.builder().id("P1").name("Iphone").price(100L).status(ProductStatus.ACTIVE).build();
        when(productService.searchProductsByName("Iphone")).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/products/search").param("keyword", "Iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Iphone"));
    }

    @Test
    @DisplayName("Nên lấy được số lượng tồn kho của sản phẩm")
    void getAvailableStock_Success() throws Exception {
        when(productService.getAvailableStock("P1")).thenReturn(50);

        mockMvc.perform(get("/api/products/P1/stock"))
                .andExpect(status().isOk())
                .andExpect(content().string("50"));
    }

    @Test
    @DisplayName("Nên kiểm tra được trạng thái còn hàng của sản phẩm")
    void isProductAvailable_Success() throws Exception {
        when(productService.isProductAvailable("P1")).thenReturn(true);

        mockMvc.perform(get("/api/products/P1/availability"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
