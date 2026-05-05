package com.shopcart.order.factory;

import com.shopcart.order.dto.response.OrderItemResponse;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import static com.shopcart.order.factory.OrderTestConstants.*;

public final class OrderResponseFactory {

    private OrderResponseFactory() {
    }

    public static OrderResponse orderResponse(String orderId) {
        return OrderResponse.builder()
                .id(orderId)
                .build();
    }

    public static OrderResponse defaultOrderResponse() {
        return orderResponse(TEST_ORDER_ID);
    }

    public static OrderItemResponse orderItemResponse(Long id, String productId, int quantity, long price) {
        return OrderItemResponse.builder()
                .id(id)
                .productId(productId)
                .quantity(quantity)
                .price(price)
                .build();
    }

    public static OrderItemResponse defaultOrderItemResponse() {
        return orderItemResponse(1L, TEST_PRODUCT_ID, 2, 125_000L);
    }

    public static OrderResponse fullOrderResponse(String orderId, String userId) {
        return OrderResponse.builder()
                .id(orderId)
                .userId(userId)
                .items(List.of(defaultOrderItemResponse()))
                .subtotal(250_000L)
                .discountAmount(0L)
                .shippingFee(0L)
                .totalPrice(250_000L)
                .status(OrderStatus.PENDING.name())
                .shippingAddress(SHIPPING_ADDRESS)
                .city(CITY)
                .district(DISTRICT)
                .ward(WARD)
                .postalCode(POSTAL_CODE)
                .phoneNumber(PHONE_NUMBER)
                .createdAt(LocalDateTime.of(2026, 5, 3, 12, 0, 0))
                .build();
    }

    public static OrderResponse defaultFullOrderResponse() {
        return fullOrderResponse(TEST_ORDER_ID, TEST_USER_ID);
    }
}
