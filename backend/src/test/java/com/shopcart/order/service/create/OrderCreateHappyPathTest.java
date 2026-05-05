package com.shopcart.order.service.create;

import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Tạo đơn hàng (Luồng thành công)")
class OrderCreateHappyPathTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC2: Tạo đơn hàng thành công khi không sử dụng mã giảm giá")
    void createOrderWithoutCoupon() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(testProductId)
                .quantity(2)
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
                .couponCode(null)
                .build();

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .totalPrice(229_900L)
                .shippingFee(29_900L)
                .discountAmount(0L)
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder().id(testOrderId).build();

        when(productService.getProductById(testProductId)).thenReturn(null);
        when(inventoryService.hasEnoughStock(testProductId, 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse response = orderService.createOrder(request, testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testOrderId);
        verify(inventoryService).reserveStock(testProductId, 2);
        verify(cartService).clearCart(testUserId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("TC3: Tạo đơn hàng thành công khi mã giảm giá hợp lệ và áp dụng giảm giá")
    void createOrderWithValidCoupon() {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(testProductId)
                .quantity(1)
                .price(1_000_000L)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderItems(List.of(itemRequest))
                .couponCode("DISCOUNT50")
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
                .totalPrice(499_900L)
                .discountAmount(500_000L)
                .couponCode("DISCOUNT50")
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder().id(testOrderId).build();

        when(productService.getProductById(testProductId)).thenReturn(null);
        when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
        when(couponService.calculateDiscount("DISCOUNT50", 1_000_000L)).thenReturn(500_000L);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expectedResponse);

        OrderResponse response = orderService.createOrder(request, testUserId);

        assertThat(response).isNotNull();
        verify(inventoryService).reserveStock(testProductId, 1);
        verify(couponService).calculateDiscount("DISCOUNT50", 1_000_000L);
        verify(cartService).clearCart(testUserId);
    }

    @Test
    @DisplayName("TC4: Tạo đơn hàng thành công với nhiều dòng sản phẩm")
    void createOrderWithMultipleItems() {
        OrderItemRequest item1 = OrderItemRequest.builder()
                .productId("product-1")
                .quantity(2)
                .price(50_000L)
                .build();

        OrderItemRequest item2 = OrderItemRequest.builder()
                .productId("product-2")
                .quantity(3)
                .price(75_000L)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderItems(List.of(item1, item2))
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

        when(productService.getProductById(anyString())).thenReturn(null);
        when(inventoryService.hasEnoughStock(anyString(), anyInt())).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toOrderResponse(any())).thenReturn(OrderResponse.builder().id(testOrderId).build());

        orderService.createOrder(request, testUserId);

        verify(inventoryService).reserveStock("product-1", 2);
        verify(inventoryService).reserveStock("product-2", 3);
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderItems()).hasSize(2);
    }
}
