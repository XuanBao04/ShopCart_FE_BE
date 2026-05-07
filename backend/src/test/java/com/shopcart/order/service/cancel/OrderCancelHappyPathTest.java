package com.shopcart.order.service.cancel;

import com.shopcart.order.entity.Order;
import com.shopcart.order.entity.OrderItem;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.order.factory.OrderEntityFactory;


import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Hủy đơn hàng (Luồng thành công)")
class OrderCancelHappyPathTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC2: Hủy đơn thành công khi đơn đang ở trạng thái PENDING")
    void cancelOrder_WhenStatusIsPending_ShouldSucceed() {
        OrderItem item = OrderEntityFactory.orderItem("product-1", 2, 100_000L);
        Order order = OrderEntityFactory.order(testOrderId, testUserId, OrderStatus.PENDING, List.of(item));

        when(orderRepository.findByIdWithItems(testOrderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(testOrderId);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(inventoryService).releaseStock("product-1", 2);
        verify(orderRepository).save(any(Order.class));
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

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, mockOrder.getStatus());
        verify(inventoryService).releaseStock("PROD_1", 2);
        verify(inventoryService).releaseStock("PROD_2", 5);
        verify(inventoryService).releaseStock("PROD_3", 1);
    }
}
