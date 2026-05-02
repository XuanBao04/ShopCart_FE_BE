package com.shopcart.service.order;

import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.exception.BusinessLogicException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TC1: Kiểm thử đơn vị chức năng cập nhật đơn hàng")
class OrderUpdateTest {

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

    private String testOrderId;

    @BeforeEach
    void setUp() {
        testOrderId = "order-789";
    }

    @Nested
    @DisplayName("TC2: cancelOrder() — hủy đơn hàng")
    class CancelOrderTests {

        @Test
        @DisplayName("TC3: Hủy đơn thành công khi đơn đang ở trạng thái PENDING")
        void cancelOrder_WhenStatusIsPending_ShouldSucceed() {
            OrderItem item = OrderTestFactory.orderItem("product-1", 2, 100_000L);
            Order order = OrderTestFactory.order(testOrderId, OrderTestFactory.TEST_USER_ID, OrderStatus.PENDING, List.of(item));

            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toOrderResponse(order)).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.cancelOrder(testOrderId);

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            verify(inventoryService).releaseStock("product-1", 2);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("TC4: Ném lỗi khi hủy đơn ở trạng thái SHIPPED")
        void cancelOrder_WhenStatusIsShipped_ShouldThrowException() {
            Order order = OrderTestFactory.order(testOrderId, OrderTestFactory.TEST_USER_ID, OrderStatus.SHIPPED, List.of());
            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

            BusinessLogicException exception = assertThrows(
                    BusinessLogicException.class,
                    () -> orderService.cancelOrder(testOrderId)
            );

            assertEquals("Cannot cancel order with status: SHIPPED", exception.getMessage());
            verify(orderRepository, never()).save(any(Order.class));
            verify(inventoryService, never()).releaseStock(anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("TC5: updateOrderStatus() — xác nhận tồn kho")
    class UpdateOrderStatusTests {

            @Test
            @DisplayName("TC6: Xác nhận tồn kho khi chuyển từ PENDING sang CONFIRMED")
            void confirmStockWhenStatusToConfirmed() {
                    OrderItem item1 = OrderItem.builder()
                                    .productId("product-1")
                                    .quantity(2)
                                    .build();

                    Order order = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.PENDING)
                                    .orderItems(List.of(item1))
                                    .build();

                    Order confirmedOrder = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.CONFIRMED)
                                    .build();

                    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
                    when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);
                    when(orderMapper.toOrderResponse(confirmedOrder))
                                    .thenReturn(OrderResponse.builder().id(testOrderId).build());

                    orderService.updateOrderStatus(testOrderId, "CONFIRMED");

                    verify(inventoryService).confirmStock("product-1", 2);
                    verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
            }

            @Test
            @DisplayName("TC7: Cập nhật trạng thái mà không xác nhận tồn kho khi đơn không còn PENDING")
            void updateStatusWithoutConfirmingStock() {
                    Order order = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.CONFIRMED)
                                    .orderItems(List.of())
                                    .build();

                    Order shippingOrder = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.SHIPPED)
                                    .build();

                    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
                    when(orderRepository.save(any(Order.class))).thenReturn(shippingOrder);
                    when(orderMapper.toOrderResponse(shippingOrder))
                                    .thenReturn(OrderResponse.builder().id(testOrderId).build());

                    orderService.updateOrderStatus(testOrderId, "SHIPPED");

                    verify(inventoryService, never()).confirmStock(anyString(), anyInt());
            }

            @Test
            @DisplayName("TC8: Ném lỗi khi định dạng trạng thái không hợp lệ")
            void throwExceptionForInvalidStatus() {
                    Order order = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.PENDING)
                                    .build();

                    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

                    assertThatThrownBy(() -> orderService.updateOrderStatus(testOrderId, "INVALID_STATUS"))
                                    .isInstanceOf(BusinessLogicException.class)
                                    .hasMessageContaining("Invalid order status");

                    verify(orderRepository, never()).save(any());
            }

            @Test
            @DisplayName("TC9: Ném lỗi khi không tìm thấy đơn trong quá trình cập nhật trạng thái")
            void throwExceptionWhenOrderNotFoundDuringUpdate() {
                    when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

                    assertThatThrownBy(() -> orderService.updateOrderStatus(testOrderId, "CONFIRMED"))
                                    .isInstanceOf(ResourceNotFoundException.class);
            }

            @Test
            @DisplayName("TC10: Không xác nhận tồn kho khi đơn không PENDING nhưng chuyển sang CONFIRMED")
            void updateOrderStatus_ToConfirmed_WhenOrderIsNotPending_ShouldNotConfirmStock() {
                    OrderItem dummyItem = OrderItem.builder()
                                    .productId("PROD_TEST")
                                    .quantity(1)
                                    .build();

                    Order order = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.CANCELLED)
                                    .orderItems(List.of(dummyItem))
                                    .build();

                    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
                    when(orderRepository.save(any())).thenReturn(order);

                    orderService.updateOrderStatus(testOrderId, "CONFIRMED");

                    verify(inventoryService, never()).confirmStock(anyString(), anyInt());
            }

            @Test
            @DisplayName("TC11: Xác nhận tồn kho cho nhiều dòng hàng khi chuyển sang CONFIRMED")
            void confirmStockForMultipleItems() {
                    OrderItem item1 = OrderItem.builder().productId("p1").quantity(2).build();
                    OrderItem item2 = OrderItem.builder().productId("p2").quantity(3).build();
                    OrderItem item3 = OrderItem.builder().productId("p3").quantity(1).build();

                    Order order = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.PENDING)
                                    .orderItems(List.of(item1, item2, item3))
                                    .build();

                    Order confirmedOrder = Order.builder()
                                    .id(testOrderId)
                                    .status(OrderStatus.CONFIRMED)
                                    .build();

                    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
                    when(orderRepository.save(any())).thenReturn(confirmedOrder);
                    when(orderMapper.toOrderResponse(any()))
                                    .thenReturn(OrderResponse.builder().id(testOrderId).build());

                    orderService.updateOrderStatus(testOrderId, "CONFIRMED");

                    verify(inventoryService).confirmStock("p1", 2);
                    verify(inventoryService).confirmStock("p2", 3);
                    verify(inventoryService).confirmStock("p3", 1);
            }

            @Test
            @DisplayName("TC12: Hủy đơn thành công khi đơn đang PENDING (kiểm thử trong nhóm cập nhật trạng thái)")
            void cancelOrder_WhenStatusIsPending_ShouldSucceed() {
                    // Arrange
                    String orderId = "ORDER_123";
                    Order mockOrder = Order.builder()
                                    .id(orderId)
                                    .status(OrderStatus.PENDING)
                                    .orderItems(List.of(OrderItem.builder().productId("PROD_1").quantity(2).build()))
                                    .build();

                    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
                    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

                    // Act
                    orderService.cancelOrder(orderId);

                    // Assert
                    assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
                    verify(inventoryService).releaseStock("PROD_1", 2);
            }

            @Test
            @DisplayName("TC13: Ném lỗi khi đơn đã ở trạng thái DELIVERED")
            void cancelOrder_WhenStatusIsDelivered_ShouldThrowException() {
                    // Arrange
                    String orderId = "ORDER_123";
                    Order mockOrder = Order.builder()
                                    .id(orderId)
                                    .status(OrderStatus.DELIVERED) // Phủ nhánh DELIVERED
                                    .build();

                    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

                    // Act & Assert
                    BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                            orderService.cancelOrder(orderId);
                    });

                    assertEquals("Cannot cancel order with status: DELIVERED", exception.getMessage());
            }

            @Test
            @DisplayName("TC14: Ném lỗi khi đơn đã ở trạng thái CANCELLED")
            void cancelOrder_WhenStatusIsCancelled_ShouldThrowException() {
                    // Arrange
                    String orderId = "ORDER_123";
                    Order mockOrder = Order.builder()
                                    .id(orderId)
                                    .status(OrderStatus.CANCELLED) // Phủ nhánh CANCELLED
                                    .build();

                    when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

                    // Act & Assert
                    BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                            orderService.cancelOrder(orderId);
                    });

                    assertEquals("Cannot cancel order with status: CANCELLED", exception.getMessage());
            }
    }
    @Test
    @DisplayName("TC15: Hủy đơn thành công khi đơn CONFIRMED — không hoàn trả kho đã giữ")
    void cancelOrder_WhenStatusIsConfirmed_ShouldSucceedWithoutReleasingStock() {
            // Arrange
            String orderId = "ORDER_456";
            Order mockOrder = Order.builder()
                            .id(orderId)
                            .status(OrderStatus.CONFIRMED)
                            .orderItems(List.of(OrderItem.builder().productId("PROD_1").quantity(2).build()))
                            .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            orderService.cancelOrder(orderId);

            // Assert
            assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());

            // Quan trọng: Vì không phải PENDING nên KHÔNG ĐƯỢC gọi hoàn kho
            verify(inventoryService, never()).releaseStock(anyString(), anyInt());
    }
    @Test
    @DisplayName("TC16: Ném ResourceNotFoundException khi đơn không tồn tại")
    void cancelOrder_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        String orderId = "NON_EXISTENT_ORDER";
        
        // Giả lập: Không tìm thấy đơn hàng trong Database -> Trả về Empty
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.cancelOrder(orderId);
        });

        // Xác minh message lỗi khớp với code Service của bạn
        assertEquals("Order not found with id: " + orderId, exception.getMessage());
        
        // Đảm bảo không có bất kỳ hành động nào phía sau được thực hiện
        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryService, never()).releaseStock(anyString(), anyInt());
    }
}
