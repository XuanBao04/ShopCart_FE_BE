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

        List<OrderItemResponse> itemResponses = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(itemResponses)
                .shippingFee(order.getShippingFee())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus() != null ? order.getStatus().toString() : null)
                .createdDate(order.getCreatedDate())
                .lastModifiedDate(order.getLastModifiedDate())
                .build();
    }

   
    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }

   
    public OrderItem toEntity(OrderItemResponse response) {
        return OrderItem.builder()
                .id(response.getId())
                .productId(response.getProductId())
                .quantity(response.getQuantity())
                .price(response.getPrice())
                .build();
    }
}
