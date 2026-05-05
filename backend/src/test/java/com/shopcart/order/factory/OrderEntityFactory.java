package com.shopcart.order.factory;

import com.shopcart.order.entity.Order;
import com.shopcart.order.entity.OrderItem;
import com.shopcart.common.enums.OrderStatus;

import java.util.List;

import static com.shopcart.order.factory.OrderTestConstants.*;

public final class OrderEntityFactory {

    private OrderEntityFactory() {
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
}
