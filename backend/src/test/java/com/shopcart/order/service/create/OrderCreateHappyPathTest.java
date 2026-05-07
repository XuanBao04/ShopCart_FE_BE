package com.shopcart.order.service.create;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.order.factory.OrderRequestFactory;
import com.shopcart.order.factory.OrderTestConstants;
import com.shopcart.order.service.BaseOrderServiceTest;
import com.shopcart.product.entity.Product;
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
        OrderRequest request = OrderRequestFactory.orderRequestWithItems(testProductId, 2, 100_000L);

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .totalPrice(229_900L)
                .shippingFee(29_900L)
                .discountAmount(0L)
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder().id(testOrderId).build();

        Product product = Product.builder().id(testProductId).price(100_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
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
        OrderRequest request = OrderRequestFactory.orderRequestWithItemsAndCoupon(testProductId, 1, 1_000_000L, "DISCOUNT50");

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .totalPrice(499_900L)
                .discountAmount(500_000L)
                .couponCode("DISCOUNT50")
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder().id(testOrderId).build();

        Product product = Product.builder().id(testProductId).price(1_000_000L).build();
        Inventory inventory = Inventory.builder().productId(testProductId).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
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
        OrderItemRequest item1 = OrderRequestFactory.orderItemRequest("product-1", 2, 50_000L);
        OrderItemRequest item2 = OrderRequestFactory.orderItemRequest("product-2", 3, 75_000L);
        OrderRequest request = OrderRequestFactory.orderRequestWithMultipleItems(List.of(item1, item2), null, OrderTestConstants.SHIPPING_ADDRESS);

        Order savedOrder = Order.builder()
                .id(testOrderId)
                .userId(testUserId)
                .status(OrderStatus.PENDING)
                .build();

        Product product1 = Product.builder().id("product-1").price(50_000L).build();
        Product product2 = Product.builder().id("product-2").price(75_000L).build();
        Inventory inv1 = Inventory.builder().productId("product-1").quantity(100).reservedQuantity(0).build();
        Inventory inv2 = Inventory.builder().productId("product-2").quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product1, product2));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inv1, inv2));
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
