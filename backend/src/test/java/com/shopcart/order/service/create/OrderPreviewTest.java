package com.shopcart.order.service.create;

import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderPreviewResponse;
import com.shopcart.order.factory.OrderTestFactory;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        OrderRequest request = OrderTestFactory.defaultOrderRequest();
        request.setCouponCode(null);

        OrderItemRequest item = request.getOrderItems().get(0);
        long subtotal = item.getPrice() * item.getQuantity();

        when(productService.getProductById(item.getProductId())).thenReturn(null);
        when(inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())).thenReturn(true);

        OrderPreviewResponse response = orderService.previewOrder(request);

        assertEquals(0L, response.getDiscountAmount());
        assertEquals(subtotal + OrderTestFactory.DEFAULT_SHIPPING_FEE, response.getTotalPrice());
    }

    @Test
    @DisplayName("TC13: Xem trước đơn với mã giảm giá hợp lệ — áp dụng giảm giá")
    void previewOrder_WithValidCoupon_ShouldApplyDiscount() {
        OrderRequest request = OrderTestFactory.defaultOrderRequest();
        request.setCouponCode("DISCOUNT20");

        OrderItemRequest item = request.getOrderItems().get(0);
        long subtotal = item.getPrice() * item.getQuantity();

        when(productService.getProductById(item.getProductId())).thenReturn(null);
        when(inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())).thenReturn(true);
        when(couponService.calculateDiscount("DISCOUNT20", subtotal)).thenReturn(20_000L);

        OrderPreviewResponse response = orderService.previewOrder(request);

        assertEquals(20_000L, response.getDiscountAmount());
        assertEquals("DISCOUNT20", response.getCouponCode());
    }

    @Test
    @DisplayName("TC14: Xem trước đơn khi mã chỉ gồm khoảng trắng — không gọi tính giảm giá")
    void previewOrder_WithEmptyCoupon_ShouldCoverMissingBranch() {
        OrderRequest request = OrderTestFactory.defaultOrderRequest();
        request.setCouponCode("   ");

        OrderItemRequest item = request.getOrderItems().get(0);

        when(productService.getProductById(item.getProductId())).thenReturn(null);
        when(inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())).thenReturn(true);

        OrderPreviewResponse response = orderService.previewOrder(request);

        assertEquals(0L, response.getDiscountAmount());
        verify(couponService, never()).calculateDiscount(anyString(), anyLong());
    }
}
