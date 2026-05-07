package com.shopcart.order.service.create;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderPreviewResponse;
import com.shopcart.order.factory.OrderRequestFactory;
import com.shopcart.order.factory.OrderTestConstants;
import com.shopcart.order.service.BaseOrderServiceTest;
import com.shopcart.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Xem trước đơn hàng (Preview)")
class OrderPreviewTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC12: Xem trước đơn không mã giảm giá — tổng tiền gồm phí vận chuyển")
    void previewOrder_WithoutCoupon_ShouldReturnFullPrice() {
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();
        request.setCouponCode(null);

        OrderItemRequest item = request.getOrderItems().get(0);
        long subtotal = item.getPrice() * item.getQuantity();

        Product product = Product.builder().id(item.getProductId()).price(item.getPrice()).build();
        Inventory inventory = Inventory.builder().productId(item.getProductId()).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));

        OrderPreviewResponse response = orderService.previewOrder(request);

        assertEquals(0L, response.getDiscountAmount());
        assertEquals(subtotal + OrderTestConstants.DEFAULT_SHIPPING_FEE, response.getTotalPrice());
    }

    @Test
    @DisplayName("TC13: Xem trước đơn với mã giảm giá hợp lệ — áp dụng giảm giá")
    void previewOrder_WithValidCoupon_ShouldApplyDiscount() {
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();
        request.setCouponCode("DISCOUNT20");

        OrderItemRequest item = request.getOrderItems().get(0);
        long subtotal = item.getPrice() * item.getQuantity();

        Product product = Product.builder().id(item.getProductId()).price(item.getPrice()).build();
        Inventory inventory = Inventory.builder().productId(item.getProductId()).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));
        when(couponService.calculateDiscount("DISCOUNT20", subtotal)).thenReturn(20_000L);

        OrderPreviewResponse response = orderService.previewOrder(request);

        assertEquals(20_000L, response.getDiscountAmount());
        assertEquals("DISCOUNT20", response.getCouponCode());
    }

    @Test
    @DisplayName("TC14: Xem trước đơn khi mã chỉ gồm khoảng trắng — không gọi tính giảm giá")
    void previewOrder_WithEmptyCoupon_ShouldCoverMissingBranch() {
        OrderRequest request = OrderRequestFactory.defaultOrderRequest();
        request.setCouponCode("   ");

        OrderItemRequest item = request.getOrderItems().get(0);

        Product product = Product.builder().id(item.getProductId()).price(item.getPrice()).build();
        Inventory inventory = Inventory.builder().productId(item.getProductId()).quantity(100).reservedQuantity(0).build();

        when(productRepository.findAllById(any())).thenReturn(List.of(product));
        when(inventoryRepository.findByProductIdIn(any())).thenReturn(List.of(inventory));

        OrderPreviewResponse response = orderService.previewOrder(request);

        assertEquals(0L, response.getDiscountAmount());
        verify(couponService, never()).calculateDiscount(anyString(), anyLong());
    }
}
