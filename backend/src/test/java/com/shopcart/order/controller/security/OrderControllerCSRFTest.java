package com.shopcart.order.controller.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderItemResponse;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.factory.OrderRequestFactory;
import com.shopcart.order.factory.OrderResponseFactory;
import com.shopcart.order.factory.OrderTestConstants;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller — CSRF (Cross-Site Request Forgery) Tests")
class OrderControllerCSRFTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("CSRF TC01: POST Request mà không có CSRF Token — 403 FORBIDDEN")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCreateOrder_WithoutCSRFToken_Denied() throws Exception {
        // Arrange
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();

        // Act & Assert
        mockMvc.perform(post("/api/orders/{userId}", OrderTestConstants.TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // NOT using .with(csrf()) → CSRF token không được gửi
                .andExpect(status().isForbidden());

        // Verify service was never called
        verify(orderService, never()).createOrder(any(), any());
    }

    @Test
    @DisplayName("CSRF TC02: DELETE Request mà không có CSRF Token — 403 FORBIDDEN")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCancelOrder_WithoutCSRFToken_Denied() throws Exception {
        // Arrange: Không thêm CSRF token

        // Act & Assert
        mockMvc.perform(delete("/api/orders/{orderId}", OrderTestConstants.TEST_ORDER_ID))
                // NOT using .with(csrf())
                .andExpect(status().isForbidden());

        // Verify service was never called
        verify(orderService, never()).cancelOrder(any());
    }

    @Test
    @DisplayName("CSRF TC03: PATCH Request mà không có CSRF Token — 403 FORBIDDEN")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateOrderStatus_WithoutCSRFToken_Denied() throws Exception {
        // Arrange: Không thêm CSRF token

        // Act & Assert
        mockMvc.perform(patch("/api/orders/{orderId}/{status}", OrderTestConstants.TEST_ORDER_ID, "CONFIRMED"))
                // NOT using .with(csrf())
                .andExpect(status().isForbidden());

        // Verify service was never called
        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    @Test
    @DisplayName("CSRF TC04: POST Request với CSRF Token hợp lệ — 201 CREATED")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCreateOrder_WithValidCSRFToken_Success() throws Exception {
        // Arrange
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();
        OrderResponse response = OrderResponseFactory.defaultFullOrderResponse();

        when(orderService.createOrder(any(OrderRequest.class), eq(OrderTestConstants.TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders/{userId}", OrderTestConstants.TEST_USER_ID)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.userId").value(OrderTestConstants.TEST_USER_ID));

        // Verify service was called
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(OrderTestConstants.TEST_USER_ID));
    }

    @Test
    @DisplayName("CSRF TC05: DELETE Request với CSRF Token hợp lệ — 200 OK")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCancelOrder_WithValidCSRFToken_Success() throws Exception {
        // Arrange
        OrderResponse cancelledOrder = OrderResponseFactory.defaultFullOrderResponse();
        cancelledOrder.setStatus("CANCELLED");

        when(orderService.cancelOrder(OrderTestConstants.TEST_ORDER_ID)).thenReturn(cancelledOrder);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/{orderId}", OrderTestConstants.TEST_ORDER_ID)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify service was called
        verify(orderService, times(1)).cancelOrder(OrderTestConstants.TEST_ORDER_ID);
    }

    @Test
    @DisplayName("CSRF TC06: PATCH Request với CSRF Token hợp lệ — 200 OK")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateOrderStatus_WithValidCSRFToken_Success() throws Exception {
        // Arrange
        String newStatus = "CONFIRMED";
        OrderResponse updatedOrder = OrderResponseFactory.defaultFullOrderResponse();
        updatedOrder.setStatus(newStatus);

        when(orderService.updateOrderStatus(OrderTestConstants.TEST_ORDER_ID, newStatus)).thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/{orderId}/{status}", OrderTestConstants.TEST_ORDER_ID, newStatus)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Verify service was called
        verify(orderService, times(1)).updateOrderStatus(OrderTestConstants.TEST_ORDER_ID, newStatus);
    }

    @Test
    @DisplayName("CSRF TC07: GET Request không cần CSRF Token — 200 OK (Safe Method)")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testGetOrder_GET_NoCSRFRequired() throws Exception {
        // Arrange: GET request không yêu cầu CSRF token
        OrderResponse response = OrderResponseFactory.defaultFullOrderResponse();
        when(orderService.getOrderById(OrderTestConstants.TEST_ORDER_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}", OrderTestConstants.TEST_ORDER_ID))
                // GET request không cần CSRF token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID));

        // Verify service was called
        verify(orderService, times(1)).getOrderById(OrderTestConstants.TEST_ORDER_ID);
    }
}
