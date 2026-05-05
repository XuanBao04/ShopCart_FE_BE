package com.shopcart.order.service.create;

import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Tạo đơn hàng (Trường hợp lỗi)")
class OrderCreateExceptionTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC16: Ném lỗi khi danh sách sản phẩm đặt mua rỗng")
    void throwExceptionWhenOrderItemsEmpty() {
        OrderRequest request = OrderRequest.builder()
                .orderItems(new ArrayList<>())
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("must contain at least one item");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC17: Ném lỗi khi danh sách sản phẩm đặt mua null")
    void throwExceptionWhenOrderItemsNull() {
        OrderRequest request = OrderRequest.builder()
                .orderItems(null)
                .build();

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(BusinessLogicException.class);
    }

    @Test
    @DisplayName("TC18: Ném lỗi khi tồn kho không đủ")
    void throwExceptionWhenInsufficientStock() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(testProductId)
                .quantity(100)
                .price(100_000L)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderItems(List.of(itemRequest))
                .shippingAddress("123 Main St")
                .city("Hanoi")
                .district("Ba Dinh")
                .ward("Truc Bach")
                .postalCode("10000")
                .phoneNumber("0912345678")
                .build();

        when(productService.getProductById(testProductId)).thenReturn(null);
        when(inventoryService.hasEnoughStock(testProductId, 100)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC19: Ném lỗi khi không tìm thấy sản phẩm")
    void throwExceptionWhenProductNotFound() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(testProductId)
                .quantity(1)
                .price(100_000L)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderItems(List.of(itemRequest))
                .build();

        when(productService.getProductById(testProductId))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC20: Ném lỗi khi giữ kho (reserve) thất bại giữa giao dịch")
    void createOrder_WhenReserveStockFails_ShouldThrowException() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(testProductId)
                .quantity(5)
                .price(100_000L)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderItems(List.of(itemRequest))
                .shippingAddress("123 Main St")
                .city("Hanoi")
                .district("Ba Dinh")
                .ward("Truc Bach")
                .postalCode("10000")
                .phoneNumber("0912345678")
                .build();

        when(productService.getProductById(testProductId)).thenReturn(null);
        when(inventoryService.hasEnoughStock(testProductId, 5)).thenReturn(true);
        doThrow(new BusinessLogicException("Stock reservation failed"))
                .when(inventoryService).reserveStock(testProductId, 5);

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Stock reservation failed");

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart(testUserId);
    }

    @Test
    @DisplayName("TC21: Giới hạn giảm giá theo tổng tiền hàng, tránh tổng thanh toán âm")
    void createOrder_WhenDiscountExceedsSubtotal_PriceShouldNotBeNegative() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(testProductId)
                .quantity(1)
                .price(100_000L)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderItems(List.of(itemRequest))
                .couponCode("SUPER_DISCOUNT")
                .shippingAddress("123 Main St")
                .city("Hanoi")
                .district("Ba Dinh")
                .ward("Truc Bach")
                .postalCode("10000")
                .phoneNumber("0912345678")
                .build();

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .status(OrderStatus.PENDING)
                .build();

        when(productService.getProductById(testProductId)).thenReturn(null);
        when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
        when(couponService.calculateDiscount("SUPER_DISCOUNT", 100_000L)).thenReturn(150_000L);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

        orderService.createOrder(request, testUserId);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order capturedOrder = captor.getValue();

        assertThat(capturedOrder.getDiscountAmount()).isEqualTo(100_000L);
        assertThat(capturedOrder.getTotalPrice()).isEqualTo(29_900L);
        assertThat(capturedOrder.getTotalPrice()).isGreaterThanOrEqualTo(29_900L);
    }
}
