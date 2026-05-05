package com.shopcart.order.service.get;

import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Truy vấn đơn hàng (Luồng thành công)")
class OrderGetHappyPathTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("Nên trả về đơn hàng khi tìm thấy theo ID")
    void getOrderByIdSuccess() {
        Order order = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(testOrderId)
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(expectedResponse);

        OrderResponse response = orderService.getOrderById(testOrderId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testOrderId);
    }

    @Test
    @DisplayName("Nên trả về danh sách đơn hàng của người dùng")
    void getUserOrdersSuccess() {
        Order order1 = Order.builder().id("order-1").userId(testUserId).build();
        List<Order> orders = List.of(order1);
        OrderResponse response1 = OrderResponse.builder().id("order-1").build();

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(orders);
        when(orderMapper.toOrderResponse(order1)).thenReturn(response1);

        List<OrderResponse> responses = orderService.getUserOrders(testUserId);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Nên trả về danh sách trống khi người dùng chưa có đơn hàng")
    void getUserOrdersEmpty() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(new ArrayList<>());
        List<OrderResponse> responses = orderService.getUserOrders(testUserId);
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Nên trả về tất cả đơn hàng trong hệ thống")
    void getAllOrdersSuccess() {
        Order order1 = Order.builder().id("order-1").build();
        when(orderRepository.findAll()).thenReturn(List.of(order1));
        when(orderMapper.toOrderResponse(order1)).thenReturn(OrderResponse.builder().id("order-1").build());

        List<OrderResponse> responses = orderService.getAllOrders();

        assertThat(responses).hasSize(1);
    }
}
