package com.shopcart.product.controller;

import com.shopcart.product.entity.Product;
import com.shopcart.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProductControllerSQLiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        productRepository.save(Product.builder().id("1").name("iPhone 15").price(1000L).status(com.shopcart.common.enums.ProductStatus.ACTIVE).build());
        productRepository.save(Product.builder().id("2").name("Samsung S24").price(900L).status(com.shopcart.common.enums.ProductStatus.ACTIVE).build());
    }

    @Test
    @DisplayName("Kiểm tra SQL Injection trên Endpoint Dễ bị tấn công - Nên rò rỉ tất cả dữ liệu")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testVulnerableSearch_SQLInjection() throws Exception {
        String injectionPayload = "' OR '1'='1";

        mockMvc.perform(get("/api/sqli/vulnerable/search")
                .param("name", injectionPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("iPhone 15")))
                .andExpect(jsonPath("$[1].name", is("Samsung S24")));
    }

    @Test
    @DisplayName("Kiểm tra SQL Injection trên Endpoint An toàn - Trả về kết quả trống")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testSecureSearch_SQLInjection() throws Exception {
        String injectionPayload = "' OR '1'='1";

        mockMvc.perform(get("/api/sqli/secure/search")
                .param("name", injectionPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
