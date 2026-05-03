package com.shopcart.order.service;

import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.order.data.OrderTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Cancel order")
class OrderCancelTest extends BaseOrderServiceTest {

    @Nested
    @DisplayName("TC1: cancelOrder() — hủy đơn hàng")
    class CancelOrderTests {

        @Test
        @DisplayName("TC2: Hủy đơn thành công khi đơn đang ở trạng thái PENDING")
        void cancelOrder_WhenStatusIsPending_ShouldSucceed() {
            OrderItem item = OrderTestFactory.orderItem("product-1", 2, 100_000L);
            Order order = OrderTestFactory.order(testOrderId, OrderTestFactory.TEST_USER_ID, OrderStatus.PENDING, List.of(item));

            when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toOrderResponse(order)).thenReturn(null);

            orderService.cancelOrder(testOrderId);

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            verify(inventoryService).releaseStock("product-1", 2);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("TC3: Ném lỗi khi hủy đơn ở trạng thái SHIPPED")
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

        @Test
        @DisplayName("TC4: Hủy đơn thành công khi đơn CONFIRMED — không hoàn trả kho đã giữ")
        void cancelOrder_WhenStatusIsConfirmed_ShouldSucceedWithoutReleasingStock() {
            String orderId = "ORDER_456";
            Order mockOrder = Order.builder()
                    .id(orderId)
                    .status(OrderStatus.CONFIRMED)
                    .orderItems(List.of(OrderItem.builder().productId("PROD_1").quantity(2).build()))
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

            orderService.cancelOrder(orderId);

            assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());

            // Quan trọng: Vì không phải PENDING nên KHÔNG ĐƯỢC gọi hoàn kho
            verify(inventoryService, never()).releaseStock(anyString(), anyInt());
        }

        @Test
        @DisplayName("TC5: Ném lỗi khi đơn đã ở trạng thái DELIVERED")
        void cancelOrder_WhenStatusIsDelivered_ShouldThrowException() {
            String orderId = "ORDER_123";
            Order mockOrder = Order.builder()
                    .id(orderId)
                    .status(OrderStatus.DELIVERED)
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

            BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                orderService.cancelOrder(orderId);
            });

            assertEquals("Cannot cancel order with status: DELIVERED", exception.getMessage());
        }

        @Test
        @DisplayName("TC6: Ném lỗi khi đơn đã ở trạng thái CANCELLED")
        void cancelOrder_WhenStatusIsCancelled_ShouldThrowException() {
            String orderId = "ORDER_123";
            Order mockOrder = Order.builder()
                    .id(orderId)
                    .status(OrderStatus.CANCELLED)
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

            BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
                orderService.cancelOrder(orderId);
            });

            assertEquals("Cannot cancel order with status: CANCELLED", exception.getMessage());
        }

        @Test
        @DisplayName("TC7: Ném ResourceNotFoundException khi đơn không tồn tại")
        void cancelOrder_WhenOrderNotFound_ShouldThrowException() {
            String orderId = "NON_EXISTENT_ORDER";

            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                orderService.cancelOrder(orderId);
            });

            assertEquals("Order not found with id: " + orderId, exception.getMessage());

            verify(orderRepository, never()).save(any(Order.class));
            verify(inventoryService, never()).releaseStock(anyString(), anyInt());
        }

        @Test
        @DisplayName("TC8: Hủy đơn thành công với nhiều dòng sản phẩm khi ở trạng thái PENDING")
        void cancelOrder_WithMultipleItems_WhenPending_ShouldReleaseAllStock() {
            String orderId = "ORDER_789";
            OrderItem item1 = OrderItem.builder().productId("PROD_1").quantity(2).build();
            OrderItem item2 = OrderItem.builder().productId("PROD_2").quantity(5).build();
            OrderItem item3 = OrderItem.builder().productId("PROD_3").quantity(1).build();

            Order mockOrder = Order.builder()
                    .id(orderId)
                    .status(OrderStatus.PENDING)
                    .orderItems(List.of(item1, item2, item3))
                    .build();

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

            orderService.cancelOrder(orderId);

            assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
            verify(inventoryService).releaseStock("PROD_1", 2);
            verify(inventoryService).releaseStock("PROD_2", 5);
            verify(inventoryService).releaseStock("PROD_3", 1);
        }
    }
}
