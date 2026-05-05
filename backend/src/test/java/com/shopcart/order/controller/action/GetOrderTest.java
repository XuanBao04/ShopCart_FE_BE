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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Order Controller — Get Order")
class GetOrderTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("TC04: Lấy đơn hàng theo ID thành công — 200 OK")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testGetOrderById_success() throws Exception {
        // Arrange
        OrderResponse response = OrderResponseFactory.defaultFullOrderResponse();

        when(orderService.getOrderById(OrderTestConstants.TEST_ORDER_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}", OrderTestConstants.TEST_ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(OrderTestConstants.TEST_ORDER_ID))
                .andExpect(jsonPath("$.userId").value(OrderTestConstants.TEST_USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(250000))
                .andExpect(jsonPath("$.items").isArray());

        // Verify service interaction
        verify(orderService, times(1)).getOrderById(OrderTestConstants.TEST_ORDER_ID);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC05: Lấy đơn hàng không tồn tại — 404 NOT FOUND")
    @WithMockUser(username = OrderTestConstants.TEST_USER_ID, roles = "USER")
    void testGetOrderById_notFound() throws Exception {
        // Arrange
        String nonExistentId = "order-999";

        when(orderService.getOrderById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Order not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/orders/{orderId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id: order-999"));

        // Verify service interaction
        verify(orderService, times(1)).getOrderById(nonExistentId);
    }
}
