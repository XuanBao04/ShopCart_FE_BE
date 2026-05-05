package com.shopcart.order.service.cancel;

import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.order.factory.OrderTestFactory;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("Order Service — Hủy đơn hàng (Trường hợp lỗi)")
class OrderCancelExceptionTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC3: Ném lỗi khi hủy đơn ở trạng thái SHIPPED")
    void cancelOrder_WhenStatusIsShipped_ShouldThrowException() {
        Order order = OrderTestFactory.order(testOrderId, testUserId, OrderStatus.SHIPPED, List.of());
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
}
