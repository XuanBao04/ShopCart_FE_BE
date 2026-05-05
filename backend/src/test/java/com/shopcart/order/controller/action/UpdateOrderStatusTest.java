package com.shopcart.order.controller.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.order.dto.response.OrderItemResponse;
import com.shopcart.order.dto.response.OrderResponse;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller — Update Order Status")
class UpdateOrderStatusTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("TC08: Cập nhật trạng thái đơn hàng thành công — 200 OK (ADMIN)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateOrderStatus_success() throws Exception {
        // Arrange
        String newStatus = "CONFIRMED";
        OrderResponse updatedOrder = OrderResponseFactory.defaultFullOrderResponse();
        updatedOrder.setStatus(newStatus);

        when(orderService.updateOrderStatus(OrderTestConstants.TEST_ORDER_ID, newStatus)).thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/{orderId}/{status}", OrderTestConstants.TEST_ORDER_ID, newStatus)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Verify service interaction
        verify(orderService, times(1)).updateOrderStatus(OrderTestConstants.TEST_ORDER_ID, newStatus);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC09: USER không có quyền cập nhật trạng thái → 403 FORBIDDEN")
    @WithMockUser(username = "user123", roles = "USER")
    void testUpdateOrderStatus_forbidden() throws Exception {
        // Arrange: USER role không có quyền cập nhật status

        // Act & Assert
        mockMvc.perform(patch("/api/orders/{orderId}/{status}", OrderTestConstants.TEST_ORDER_ID, "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Verify service was never called
        verify(orderService, never()).updateOrderStatus(anyString(), anyString());
    }
}
