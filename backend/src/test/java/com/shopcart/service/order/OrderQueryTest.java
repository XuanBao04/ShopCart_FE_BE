package com.shopcart.service.order;

import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.mapper.OrderMapper;
import com.shopcart.repository.OrderRepository;
import com.shopcart.service.ICartService;
import com.shopcart.service.ICouponService;
import com.shopcart.service.IInventoryService;
import com.shopcart.service.IProductService;
import com.shopcart.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TC1: Kiểm thử đơn vị chức năng truy vấn đơn hàng")
class OrderQueryTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private IInventoryService inventoryService;

    @Mock
    private IProductService productService;

    @Mock
    private ICartService cartService;

    @Mock
    private ICouponService couponService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private String testUserId;
    private String testOrderId;

    @BeforeEach
    void setUp() {
        testUserId = "user-123";
        testOrderId = "order-789";
    }

    @Nested
    @DisplayName("TC2: getOrderById() — lấy đơn hàng theo mã")
    class GetOrderByIdTests {

        @Test
        @DisplayName("TC3: Trả về đơn hàng khi tìm thấy theo mã định danh")
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
            verify(orderRepository).findById(testOrderId);
        }

        @Test
        @DisplayName("TC4: Ném lỗi khi không tìm thấy đơn hàng")
        void throwExceptionWhenOrderNotFound() {
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(testOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Order not found");
        }
    }

    @Nested
    @DisplayName("TC5: getUserOrders() — truy vấn danh sách đơn của người dùng")
    class GetUserOrdersTests {

        @Test
        @DisplayName("TC6: Trả về đầy đủ danh sách đơn của người dùng")
        void getUserOrdersSuccess() {
            Order order1 = Order.builder().id("order-1").userId(testUserId).build();
            Order order2 = Order.builder().id("order-2").userId(testUserId).build();
            List<Order> orders = List.of(order1, order2);

            OrderResponse response1 = OrderResponse.builder().id("order-1").build();
            OrderResponse response2 = OrderResponse.builder().id("order-2").build();

            when(orderRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(orders);
            when(orderMapper.toOrderResponse(order1)).thenReturn(response1);
            when(orderMapper.toOrderResponse(order2)).thenReturn(response2);

            List<OrderResponse> responses = orderService.getUserOrders(testUserId);

            assertThat(responses).hasSize(2);
            assertThat(responses).extracting("id").containsExactly("order-1", "order-2");
        }

        @Test
        @DisplayName("TC7: Trả về danh sách rỗng khi người dùng chưa có đơn")
        void getUserOrdersEmpty() {
            when(orderRepository.findByUserIdOrderByCreatedAtDesc(testUserId)).thenReturn(new ArrayList<>());

            List<OrderResponse> responses = orderService.getUserOrders(testUserId);

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("TC8: getAllOrders() — truy vấn tất cả đơn hàng")
    class GetAllOrdersTests {

        @Test
        @DisplayName("TC9: Trả về danh sách tất cả đơn hàng")
        void getAllOrdersSuccess() {
            Order order1 = Order.builder().id("order-1").build();
            Order order2 = Order.builder().id("order-2").build();
            List<Order> orders = List.of(order1, order2);

            OrderResponse response1 = OrderResponse.builder().id("order-1").build();
            OrderResponse response2 = OrderResponse.builder().id("order-2").build();

            when(orderRepository.findAll()).thenReturn(orders);
            when(orderMapper.toOrderResponse(order1)).thenReturn(response1);
            when(orderMapper.toOrderResponse(order2)).thenReturn(response2);

            List<OrderResponse> responses = orderService.getAllOrders();

            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("TC10: Trả về danh sách rỗng khi hệ thống chưa có đơn")
        void getAllOrdersEmpty() {
            when(orderRepository.findAll()).thenReturn(new ArrayList<>());

            List<OrderResponse> responses = orderService.getAllOrders();

            assertThat(responses).isEmpty();
        }
    }
}
