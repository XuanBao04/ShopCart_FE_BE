package com.shopcart.order.controller.action;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller — Cancel Order")
class CancelOrderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("TC06: Hủy đơn hàng thành công — 200 OK")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCancelOrder_success() throws Exception {
        // Arrange
        OrderResponse cancelledOrder = OrderResponseFactory.defaultFullOrderResponse();
        cancelledOrder.setStatus("CANCELLED");

        when(orderService.cancelOrder(OrderTestConstants.TEST_ORDER_ID)).thenReturn(cancelledOrder);

        // Act & Assert
        mockMvc.perform(delete("/api/orders/{orderId}", OrderTestConstants.TEST_ORDER_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify service interaction
        verify(orderService, times(1)).cancelOrder(OrderTestConstants.TEST_ORDER_ID);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC07: Hủy đơn hàng không tồn tại — 404 NOT FOUND")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testCancelOrder_notFound() throws Exception {
        // Arrange
        String nonExistentId = "order-999";

        when(orderService.cancelOrder(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Order not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(delete("/api/orders/{orderId}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: order-999"));

        // Verify service interaction
        verify(orderService, times(1)).cancelOrder(nonExistentId);
    }
}
