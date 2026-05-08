package com.shopcart.order.service.create;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.order.factory.OrderRequestFactory;
import com.shopcart.order.service.BaseOrderServiceTest;
import com.shopcart.product.entity.Product;
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
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, 100, 100_000L);

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(50).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC19: Ném lỗi khi không tìm thấy sản phẩm")
    void throwExceptionWhenProductNotFound() {
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, 1, 100_000L);

        when(productRepository.findAllById(any())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC20: Ném lỗi khi giữ kho (reserve) thất bại giữa giao dịch")
    void createOrder_WhenReserveStockFails_ShouldThrowException() {
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, 5, 100_000L);

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
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
        OrderRequest request = OrderRequestFactory.orderRequestWithItemsAndCoupon(testProductId, 1, 100_000L, "SUPER_DISCOUNT");

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .status(OrderStatus.PENDING)
                .build();

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
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
