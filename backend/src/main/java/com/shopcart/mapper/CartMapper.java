package com.shopcart.mapper;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class CartMapper {

   
    public CartResponse toCartResponse(String userId, List<CartItem> items) {
        if (items == null) {
            items = java.util.Collections.emptyList();
        }

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        Long totalPrice = items.stream()
                .mapToLong(item -> item.getQuantity())
                .sum();

        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalItems(items.size())
                .totalPrice(totalPrice)
                .build();
    }

    
    public CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .build();
    }

  
    public CartItem toEntity(CartItemResponse response) {
        return CartItem.builder()
                .id(response.getId())
                .productId(response.getProductId())
                .quantity(response.getQuantity())
                .build();
    }
}
