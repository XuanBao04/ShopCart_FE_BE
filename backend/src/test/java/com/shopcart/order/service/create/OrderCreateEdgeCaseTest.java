package com.shopcart.order.service.create;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.order.entity.OrderItem;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.order.factory.OrderEntityFactory;
import com.shopcart.order.factory.OrderRequestFactory;
import com.shopcart.order.service.BaseOrderServiceTest;
import com.shopcart.product.entity.Product;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Tạo đơn hàng (Trường hợp biên)")
class OrderCreateEdgeCaseTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC6: Không lưu đơn và ném lỗi khi mã giảm giá không hợp lệ")
    void createOrderWithInvalidCoupon() {
        OrderRequest request = OrderRequestFactory.orderRequestWithItemsAndCoupon(testProductId, 1, 100_000L, "INVALID_CODE");

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
        when(couponService.calculateDiscount("INVALID_CODE", 100_000L))
                .thenThrow(new ResourceNotFoundException("Coupon not found"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(request, "user123"));

        assertEquals("Coupon not found", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("TC7: Tiếp tục tạo đơn khi mã giảm giá là chuỗi rỗng")
    void createOrderWithEmptyCouponCode() {
        OrderRequest request = OrderRequestFactory.orderRequestWithItemsAndCoupon(testProductId, 1, 100_000L, "");

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .totalPrice(129_900L)
                .status(OrderStatus.PENDING)
                .build();

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

        orderService.createOrder(request, testUserId);

        verify(couponService, never()).calculateDiscount(anyString(), anyLong());
    }

    @Test
    @DisplayName("TC8: Tạo đơn hàng chỉ với một dòng sản phẩm")
    void createOrderWithSingleItem() {
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, 1, 100_000L);

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .status(OrderStatus.PENDING)
                .build();

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

        orderService.createOrder(request, testUserId);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderItems()).hasSize(1);
    }

    @Test
    @DisplayName("TC9: Ánh xạ thực thể đơn hàng khi userId bằng null")
    void createOrder_VerifyEntityMapping_AndNullUserId() {
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, 2, 50_000L);

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .status(OrderStatus.PENDING)
                .build();

        Product product = Product.builder().id(testProductId).price(50_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

        orderService.createOrder(request, null);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order capturedOrder = captor.getValue();

        assertThat(capturedOrder.getUserId()).isNull();
        assertThat(capturedOrder.getOrderItems()).isNotNull().hasSize(1);
        OrderItem capturedItem = capturedOrder.getOrderItems().get(0);
        assertThat(capturedItem.getProductId()).isEqualTo(testProductId);
        assertThat(capturedItem.getQuantity()).isEqualTo(2);
        assertThat(capturedItem.getPrice()).isEqualTo(50_000L);
        assertThat(capturedItem.getOrder()).isSameAs(capturedOrder);
    }

    @Test
    @DisplayName("TC10: Tính tổng tiền không tràn số khi số lượng bằng Integer.MAX_VALUE")
    void createOrder_WithMaxIntegerQuantity_ShouldNotOverflow() {
        long price = 100L;
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, Integer.MAX_VALUE, price);

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .status(OrderStatus.PENDING)
                .build();

        Product product = Product.builder().id(testProductId).price(price).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(Integer.MAX_VALUE).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

        orderService.createOrder(request, testUserId);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order capturedOrder = captor.getValue();

        long expectedSubtotal = (long) Integer.MAX_VALUE * price;
        long expectedTotal = expectedSubtotal + 29_900L;

        assertThat(capturedOrder.getTotalPrice()).isEqualTo(expectedTotal);
        assertThat(capturedOrder.getOrderItems()).hasSize(1);
        assertThat(capturedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(Integer.MAX_VALUE);
    }
}
