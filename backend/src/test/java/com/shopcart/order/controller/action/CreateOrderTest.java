package com.shopcart.order.controller.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderItemResponse;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.factory.OrderRequestFactory;
import com.shopcart.order.factory.OrderResponseFactory;
import com.shopcart.order.factory.OrderTestConstants;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.order.service.IOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller — Create Order")
class CreateOrderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("TC01: Tạo đơn hàng thành công — 201 CREATED")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCreateOrder_success() throws Exception {
        // Arrange
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();
        OrderResponse response = OrderResponseFactory.defaultFullOrderResponse();

        when(orderService.createOrder(any(OrderRequest.class), eq(OrderTestConstants.TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders/{userId}", OrderTestConstants.TEST_USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.userId").value(OrderTestConstants.TEST_USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(250000))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(OrderTestConstants.TEST_PRODUCT_ID))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        // Verify service interaction
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(OrderTestConstants.TEST_USER_ID));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC02: Tạo đơn hàng thiếu thông tin bắt buộc — 400 BAD REQUEST")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCreateOrder_invalidRequest() throws Exception {
        // Arrange — missing required fields (userId, orderItems, shippingAddress, etc.)
        OrderRequest invalidRequest = OrderRequest.builder()
                .userId("")        // blank → violates @NotBlank
                .orderItems(null)  // null → violates @NotEmpty
                .shippingAddress("")
                .city("")
                .district("")
                .ward("")
                .phoneNumber("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/orders/{userId}", OrderTestConstants.TEST_USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").exists());

        // Verify service was never called due to validation failure
        verify(orderService, never()).createOrder(any(OrderRequest.class), any(String.class));
    }

    @Test
    @DisplayName("TC03: Tạo đơn hàng khi hết hàng — 422 UNPROCESSABLE_ENTITY")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCreateOrder_outOfStock() throws Exception {
        // Arrange
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();

        when(orderService.createOrder(any(OrderRequest.class), eq(OrderTestConstants.TEST_USER_ID)))
                .thenThrow(new BusinessLogicException("Product " + OrderTestConstants.TEST_PRODUCT_ID + " is out of stock"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/{userId}", OrderTestConstants.TEST_USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Product " + OrderTestConstants.TEST_PRODUCT_ID + " is out of stock"));

        // Verify service was called
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(OrderTestConstants.TEST_USER_ID));
    }
}
