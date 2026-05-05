package com.shopcart.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO trả về khi preview đơn hàng trước khi checkout.
 * Cho user xem tổng tiền, phí ship, discount trước khi xác nhận đặt hàng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPreviewResponse {
    private String userId;
    private List<OrderItemResponse> items;
    private Long subtotal;         // Sum of (price * quantity)
    private Long discountAmount;   // Discount from coupon in VND
    private Long shippingFee;
    private Long totalPrice;       // subtotal - discount + shipping
    private String couponCode;     // Applied coupon code (if any)
}
