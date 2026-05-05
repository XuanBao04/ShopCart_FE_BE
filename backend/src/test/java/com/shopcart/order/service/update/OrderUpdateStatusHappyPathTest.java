package com.shopcart.order.service.update;

import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.order.entity.OrderItem;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.order.factory.OrderEntityFactory;
import com.shopcart.order.factory.OrderResponseFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.shopcart.order.service.BaseOrderServiceTest;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Cập nhật trạng thái đơn hàng (Luồng thành công)")
class OrderUpdateStatusHappyPathTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC2: Xác nhận tồn kho khi chuyển từ PENDING sang CONFIRMED")
    void confirmStockWhenStatusToConfirmed() {
        OrderItem item1 = OrderEntityFactory.orderItem("product-1", 2, 100_000L);

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
                .thenReturn(OrderResponseFactory.orderResponse(testOrderId));

        orderService.updateOrderStatus(testOrderId, "CONFIRMED");

        verify(inventoryService).confirmStock("product-1", 2);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    @Test
    @DisplayName("TC3: Cập nhật trạng thái mà không xác nhận tồn kho khi đơn không còn PENDING")
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
                .thenReturn(OrderResponseFactory.orderResponse(testOrderId));

        orderService.updateOrderStatus(testOrderId, "SHIPPED");

        verify(inventoryService, never()).confirmStock(anyString(), anyInt());
    }

    @Test
    @DisplayName("TC6: Không xác nhận tồn kho khi đơn không PENDING nhưng chuyển sang CONFIRMED")
    void updateOrderStatus_ToConfirmed_WhenOrderIsNotPending_ShouldNotConfirmStock() {
        OrderItem dummyItem = OrderEntityFactory.orderItem("PROD_TEST", 1, 100_000L);

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
    @DisplayName("TC7: Xác nhận tồn kho cho nhiều dòng hàng khi chuyển sang CONFIRMED")
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
}
