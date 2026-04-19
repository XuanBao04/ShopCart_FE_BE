package com.shopcart.mapper;

import com.shopcart.dto.response.OrderResponse;
import com.shopcart.dto.response.OrderItemResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> itemResponses = order.orderItems != null
                ? order.orderItems.stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        return new OrderResponse(
                order.id,
                order.userId,
                itemResponses,
                order.status != null ? order.status.toString() : null,
                order.createdDate,
                order.lastModifiedDate
        );
    }

   
    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(item.id, item.productId, item.quantity, item.price);
    }

   
    public OrderItem toEntity(OrderItemResponse response) {
        OrderItem item = new OrderItem();
        item.id = response.id;
        item.productId = response.productId;
        item.quantity = response.quantity;
        item.price = response.price;
        return item;
    }
}
