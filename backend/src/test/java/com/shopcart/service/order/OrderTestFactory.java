package com.shopcart.service.order;

import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.enums.OrderStatus;

import java.util.List;

public final class OrderTestFactory {

    public static final String TEST_USER_ID = "user-123";
    public static final String TEST_PRODUCT_ID = "product-456";
    public static final String TEST_ORDER_ID = "order-789";

    public static final String SHIPPING_ADDRESS = "123 Main St";
    public static final String CITY = "Hanoi";
    public static final String DISTRICT = "Ba Dinh";
    public static final String WARD = "Truc Bach";
    public static final String POSTAL_CODE = "10000";
    public static final String PHONE_NUMBER = "0912345678";

    public static final long DEFAULT_SHIPPING_FEE = 29_900L;

    private OrderTestFactory() {
    }

    public static OrderItemRequest orderItemRequest(String productId, int quantity, long price) {
        return OrderItemRequest.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .build();
    }

    public static OrderItemRequest defaultOrderItemRequest() {
        return orderItemRequest(TEST_PRODUCT_ID, 1, 100_000L);
    }

    public static OrderRequest orderRequest(List<OrderItemRequest> items, String couponCode) {
        return OrderRequest.builder()
                .orderItems(items)
                .shippingAddress(SHIPPING_ADDRESS)
                .city(CITY)
                .district(DISTRICT)
                .ward(WARD)
                .postalCode(POSTAL_CODE)
                .phoneNumber(PHONE_NUMBER)
                .couponCode(couponCode)
                .build();
    }

    public static OrderRequest defaultOrderRequest() {
        return orderRequest(List.of(defaultOrderItemRequest()), null);
    }

    public static OrderItem orderItem(String productId, int quantity, long price) {
        return OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .build();
    }

    public static Order order(String orderId, String userId, OrderStatus status, List<OrderItem> items) {
        return Order.builder()
                .id(orderId)
                .userId(userId)
                .status(status)
                .orderItems(items)
                .build();
    }

    public static Order pendingOrder() {
        OrderItem item = orderItem(TEST_PRODUCT_ID, 1, 100_000L);
        return order(TEST_ORDER_ID, TEST_USER_ID, OrderStatus.PENDING, List.of(item));
    }

    public static Order savedOrder(long totalPrice, long discountAmount, String couponCode) {
        return Order.builder()
                .id(TEST_ORDER_ID)
                .userId(TEST_USER_ID)
                .totalPrice(totalPrice)
                .shippingFee(DEFAULT_SHIPPING_FEE)
                .discountAmount(discountAmount)
                .couponCode(couponCode)
                .status(OrderStatus.PENDING)
                .build();
    }

    public static OrderResponse orderResponse(String orderId) {
        return OrderResponse.builder()
                .id(orderId)
                .build();
    }

    public static OrderResponse defaultOrderResponse() {
        return orderResponse(TEST_ORDER_ID);
    }
}
