package com.shopcart.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderItemResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.service.IOrderService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings("null")
@SpringBootTest  //  Full context with method security
@AutoConfigureMockMvc
@ActiveProfiles("test")  //  Uses application-test.yaml with H2 database
class OrderControllerLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IOrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = "user123";
    private static final String ORDER_ID = "order-001";


    @Test
    @DisplayName("TC01: Tạo đơn hàng thành công — 201 CREATED")
    @WithMockUser(username = "user123", roles = "USER")
    void testCreateOrder_success() throws Exception {
        // Arrange
        OrderRequest request = buildValidOrderRequest();
        OrderResponse response = buildOrderResponse();

        when(orderService.createOrder(any(OrderRequest.class), eq(USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/orders/{userId}", USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(250000))
                .andExpect(jsonPath("$.shippingAddress").value("123 Nguyen Hue"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value("PRD-001"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        // Verify service interaction
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(USER_ID));
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC02: Tạo đơn hàng thiếu thông tin bắt buộc — 400 BAD REQUEST")
    @WithMockUser(username = "user123", roles = "USER")
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
        mockMvc.perform(post("/orders/{userId}", USER_ID)
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
    @WithMockUser(username = "user123", roles = "USER")
    void testCreateOrder_outOfStock() throws Exception {
        // Arrange
        OrderRequest request = buildValidOrderRequest();

        when(orderService.createOrder(any(OrderRequest.class), eq(USER_ID)))
                .thenThrow(new BusinessLogicException("Product PRD-001 is out of stock"));

        // Act & Assert
        mockMvc.perform(post("/orders/{userId}", USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Product PRD-001 is out of stock"));

        // Verify service was called
        verify(orderService, times(1)).createOrder(any(OrderRequest.class), eq(USER_ID));
    }


    @Test
    @DisplayName("TC04: Lấy đơn hàng theo ID thành công — 200 OK")
    @WithMockUser(username = "user123", roles = "USER")
    void testGetOrderById_success() throws Exception {
        // Arrange
        OrderResponse response = buildOrderResponse();

        when(orderService.getOrderById(ORDER_ID)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/orders/{orderId}", ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(250000))
                .andExpect(jsonPath("$.items").isArray());

        // Verify service interaction
        verify(orderService, times(1)).getOrderById(ORDER_ID);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC05: Lấy đơn hàng không tồn tại — 404 NOT FOUND")
    @WithMockUser(username = "user123", roles = "USER")
    void testGetOrderById_notFound() throws Exception {
        // Arrange
        String nonExistentId = "order-999";

        when(orderService.getOrderById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Order not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/orders/{orderId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id: order-999"));

        // Verify service interaction
        verify(orderService, times(1)).getOrderById(nonExistentId);
    }



    @Test
    @DisplayName("TC06: Hủy đơn hàng thành công — 200 OK")
    @WithMockUser(username = "user123", roles = "USER")
    void testCancelOrder_success() throws Exception {
        // Arrange
        OrderResponse cancelledOrder = buildOrderResponse();
        cancelledOrder.setStatus("CANCELLED");

        when(orderService.cancelOrder(ORDER_ID)).thenReturn(cancelledOrder);

        // Act & Assert
        mockMvc.perform(delete("/orders/{orderId}", ORDER_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify service interaction
        verify(orderService, times(1)).cancelOrder(ORDER_ID);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    @DisplayName("TC07: Hủy đơn hàng không tồn tại — 404 NOT FOUND")
    @WithMockUser(username = "user123", roles = "USER")
    void testCancelOrder_notFound() throws Exception {
        // Arrange
        String nonExistentId = "order-999";

        when(orderService.cancelOrder(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Order not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(delete("/orders/{orderId}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found with id: order-999"));

        // Verify service interaction
        verify(orderService, times(1)).cancelOrder(nonExistentId);
    }

    

    @Test
    @DisplayName("TC08: Cập nhật trạng thái đơn hàng thành công — 200 OK (ADMIN)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testUpdateOrderStatus_success() throws Exception {
        // Arrange
        String newStatus = "CONFIRMED";
        OrderResponse updatedOrder = buildOrderResponse();
        updatedOrder.setStatus(newStatus);

        when(orderService.updateOrderStatus(ORDER_ID, newStatus)).thenReturn(updatedOrder);

        // Act & Assert
        mockMvc.perform(patch("/orders/{orderId}/{status}", ORDER_ID, newStatus)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Verify service interaction
        verify(orderService, times(1)).updateOrderStatus(ORDER_ID, newStatus);
        verifyNoMoreInteractions(orderService);
    }

   @Test
@DisplayName("TC09: USER không có quyền cập nhật trạng thái → 403 FORBIDDEN")
@WithMockUser(username = "user123", roles = "USER")
void testUpdateOrderStatus_forbidden() throws Exception {

    mockMvc.perform(patch("/orders/{orderId}/{status}", ORDER_ID, "CONFIRMED")
                    .with(csrf()))
            .andExpect(status().isForbidden());

    verify(orderService, never()).updateOrderStatus(anyString(), anyString());
}
  
    
    private OrderRequest buildValidOrderRequest() {
        OrderItemRequest item = OrderItemRequest.builder()
                .productId("PRD-001")
                .quantity(2)
                .price(125000L)
                .build();

        return OrderRequest.builder()
                .userId(USER_ID)
                .orderItems(List.of(item))
                .shippingAddress("123 Nguyen Hue")
                .city("Ho Chi Minh")
                .district("District 1")
                .ward("Ben Nghe")
                .phoneNumber("0901234567")
                .build();
    }

   
    private OrderResponse buildOrderResponse() {
        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .id(1L)
                .productId("PRD-001")
                .quantity(2)
                .price(125000L)
                .build();

        return OrderResponse.builder()
                .id(ORDER_ID)
                .userId(USER_ID)
                .items(List.of(itemResponse))
                .subtotal(250000L)
                .discountAmount(0L)
                .shippingFee(0L)
                .totalPrice(250000L)
                .status("PENDING")
                .shippingAddress("123 Nguyen Hue")
                .city("Ho Chi Minh")
                .district("District 1")
                .ward("Ben Nghe")
                .phoneNumber("0901234567")
                .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0, 0))
                .build();
    }
}
