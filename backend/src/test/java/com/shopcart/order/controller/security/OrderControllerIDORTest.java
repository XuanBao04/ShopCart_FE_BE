package com.shopcart.order.controller.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderItemResponse;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.factory.OrderResponseFactory;
import com.shopcart.order.factory.OrderTestConstants;
import com.shopcart.common.exception.ResourceNotFoundException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller — IDOR (Insecure Direct Object Reference) Tests")
class OrderControllerIDORTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_A_ID = "user123";
    private static final String USER_B_ID = "user456";
    private static final String ORDER_A_ID = "order-user-a-001";

    @Test
    @DisplayName("IDOR TC01: User B không nên lấy được Order của User A — 403 FORBIDDEN")
    @WithMockUser(username = USER_B_ID, roles = "USER")
    void testGetOrder_IDOR_UnauthorizedAccess() throws Exception {
        when(orderService.getOrderById(ORDER_A_ID))
                .thenThrow(new ResourceNotFoundException("Order not found or access denied"));

        mockMvc.perform(get("/api/orders/{orderId}", ORDER_A_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        verify(orderService, times(1)).getOrderById(ORDER_A_ID);
    }

    @Test
    @DisplayName("IDOR TC02: User B không nên hủy Order của User A — 403 FORBIDDEN")
    @WithMockUser(username = USER_B_ID, roles = "USER")
    void testCancelOrder_IDOR_UnauthorizedAccess() throws Exception {
        when(orderService.cancelOrder(ORDER_A_ID))
                .thenThrow(new ResourceNotFoundException("Order not found or access denied"));

        mockMvc.perform(delete("/api/orders/{orderId}", ORDER_A_ID)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());

        verify(orderService, times(1)).cancelOrder(ORDER_A_ID);
    }

    @Test
    @DisplayName("IDOR TC03: Regular USER không nên cập nhật Status của Order — 403 FORBIDDEN")
    @WithMockUser(username = USER_A_ID, roles = "USER")
    void testUpdateOrderStatus_IDOR_RoleBasedAccess() throws Exception {
        String newStatus = "CONFIRMED";

        mockMvc.perform(patch("/api/orders/{orderId}/{status}", ORDER_A_ID, newStatus)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(orderService, never()).updateOrderStatus(anyString(), anyString());
    }

    @Test
    @DisplayName("IDOR TC04: ADMIN có quyền cập nhật bất kỳ Order — 200 OK")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateOrderStatus_IDOR_AdminAccess() throws Exception {
        String newStatus = "CONFIRMED";
        OrderResponse updatedOrder = OrderResponseFactory.fullOrderResponse(ORDER_A_ID, "some-other-user");
        updatedOrder.setStatus(newStatus);

        when(orderService.updateOrderStatus(ORDER_A_ID, newStatus)).thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/orders/{orderId}/{status}", ORDER_A_ID, newStatus)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_A_ID))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService, times(1)).updateOrderStatus(ORDER_A_ID, newStatus);
    }

    @Test
    @DisplayName("IDOR TC05: Unauthenticated User không nên truy cập Order — 401 UNAUTHORIZED")
    void testGetOrder_Unauthenticated_Denied() throws Exception {
        mockMvc.perform(get("/api/orders/{orderId}", ORDER_A_ID))
                .andExpect(status().isUnauthorized());

        verify(orderService, never()).getOrderById(anyString());
    }

    @Test
    @DisplayName("IDOR TC06: User A có quyền lấy riêng Order của mình — 200 OK")
    @WithMockUser(username = USER_A_ID, roles = "USER")
    void testGetOrder_AuthorizedAccess_Success() throws Exception {
        OrderResponse response = OrderResponseFactory.fullOrderResponse(ORDER_A_ID, USER_A_ID);

        when(orderService.getOrderById(ORDER_A_ID)).thenReturn(response);

        mockMvc.perform(get("/api/orders/{orderId}", ORDER_A_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_A_ID))
                .andExpect(jsonPath("$.userId").value(USER_A_ID));

        verify(orderService, times(1)).getOrderById(ORDER_A_ID);
    }
}
