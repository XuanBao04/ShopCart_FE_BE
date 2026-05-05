package com.shopcart.order.factory;

import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;

import java.util.List;

import static com.shopcart.order.factory.OrderTestConstants.*;

public final class OrderRequestFactory {

    private OrderRequestFactory() {
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
                .userId(TEST_USER_ID)
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

    public static OrderRequest orderRequestWithItems(String productId, int quantity, long price) {
        return orderRequest(List.of(orderItemRequest(productId, quantity, price)), null);
    }

    public static OrderRequest orderRequestWithItems(String productId, int quantity, long price, String shippingAddress) {
        return OrderRequest.builder()
                .userId(TEST_USER_ID)
                .orderItems(List.of(orderItemRequest(productId, quantity, price)))
                .shippingAddress(shippingAddress)
                .city(CITY)
                .district(DISTRICT)
                .ward(WARD)
                .postalCode(POSTAL_CODE)
                .phoneNumber(PHONE_NUMBER)
                .build();
    }

    public static OrderRequest orderRequestWithCoupon(String couponCode) {
        return orderRequest(List.of(defaultOrderItemRequest()), couponCode);
    }

    public static OrderRequest orderRequestWithItemsAndCoupon(String productId, int quantity, long price, String couponCode) {
        return orderRequest(List.of(orderItemRequest(productId, quantity, price)), couponCode);
    }

    public static OrderRequest orderRequestWithMultipleItems(List<OrderItemRequest> items, String couponCode, String shippingAddress) {
        return OrderRequest.builder()
                .userId(TEST_USER_ID)
                .orderItems(items)
                .shippingAddress(shippingAddress)
                .city(CITY)
                .district(DISTRICT)
                .ward(WARD)
                .postalCode(POSTAL_CODE)
                .phoneNumber(PHONE_NUMBER)
                .couponCode(couponCode)
                .build();
    }
}
