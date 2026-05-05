package com.shopcart.cart.controller;

import com.shopcart.cart.controller.CartController;
import com.shopcart.cart.dto.request.CartItemRequest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.cart.service.ICartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SuppressWarnings("null")
@WebMvcTest(CartController.class)
@AutoConfigureMockMvc
class CartControllerLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_ENDPOINT = "/api/cart/{userId}/add";
    private static final String USER_ID = "user123";


    @Test
    @DisplayName("TC01: Thêm sản phẩm vào giỏ thành công  - 201 CREATED")
    @WithMockUser(username = "user123", roles = "USER")
    void testAddToCart_success() throws Exception {
        // Arrange
        CartItemRequest request = CartItemRequest.builder()
                .productId("PRD-001")
                .quantity(2)
                .build();

        CartResponse response = CartResponse.builder()
                .userId(USER_ID)
                .totalItems(2)
                .totalPrice(100000L)
                .build();

        when(cartService.addToCart(eq(USER_ID), any(CartItemRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(API_ENDPOINT, USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPrice").value(100000L));

        // Verify service 
        verify(cartService, times(1)).addToCart(eq(USER_ID), any(CartItemRequest.class));
        verifyNoMoreInteractions(cartService);
    }

    
    @Test
    @DisplayName("TC02: Thêm sản phẩm với số lượng không hợp lệ - 400 BAD REQUEST")
    @WithMockUser(username = "user123", roles = "USER")
    void testAddToCart_invalidQuantity() throws Exception {
        // Arrange
        CartItemRequest request = CartItemRequest.builder()
                .productId("PRD-001")
                .quantity(0)  
                .build();

        // Act & Assert
        mockMvc.perform(post(API_ENDPOINT, USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify service 
        verify(cartService, never()).addToCart(any(String.class), any(CartItemRequest.class));
    }

   
    @Test
    @DisplayName("TC03: Thêm sản phẩm không tồn tại - 404 NOT FOUND")
    @WithMockUser(username = "user123", roles = "USER")
    void testAddToCart_productNotFound() throws Exception {
        // Arrange
        CartItemRequest request = CartItemRequest.builder()
                .productId("PRD-NOTFOUND")
                .quantity(1)
                .build();

        when(cartService.addToCart(eq(USER_ID), any(CartItemRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        // Act & Assert
        mockMvc.perform(post(API_ENDPOINT, USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());

        // Verify service 
        verify(cartService, times(1)).addToCart(eq(USER_ID), any(CartItemRequest.class));
    }

   
    @Test
    @DisplayName("TC04: Thêm sản phẩm vượt quá số lượng tồn - 409 CONFLICT")
    @WithMockUser(username = "user123", roles = "USER")
    void testAddToCart_outOfStock() throws Exception {
        // Arrange
        CartItemRequest request = CartItemRequest.builder()
                .productId("PRD-001")
                .quantity(100)  // Quantity exceeds available stock
                .build();

        when(cartService.addToCart(eq(USER_ID), any(CartItemRequest.class)))
                .thenThrow(new BusinessLogicException("Product is out of stock"));

        // Act & Assert
        mockMvc.perform(post(API_ENDPOINT, USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());

        // Verify service was called
        verify(cartService, times(1)).addToCart(eq(USER_ID), any(CartItemRequest.class));
    }

   
    @Test
    @DisplayName("TC05: Thêm sản phẩm khi chưa đăng nhập - 401 UNAUTHORIZED")
    void testAddToCart_unauthorized() throws Exception {
        // Arrange
        CartItemRequest request = CartItemRequest.builder()
                .productId("PRD-001")
                .quantity(1)
                .build();

        // Act & Assert (No @WithMockUser)
        mockMvc.perform(post(API_ENDPOINT, USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        // Verify service 
        verify(cartService, never()).addToCart(any(String.class), any(CartItemRequest.class));
    }
}