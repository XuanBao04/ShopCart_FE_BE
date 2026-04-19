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
                .mapToLong(item -> item.quantity)
                .sum();

        return new CartResponse(userId, itemResponses, items.size(), totalPrice);
    }

    
    public CartItemResponse toCartItemResponse(CartItem item) {
        return new CartItemResponse(item.id, item.productId, item.quantity);
    }

  
    public CartItem toEntity(CartItemResponse response) {
        CartItem cartItem = new CartItem();
        cartItem.id = response.id;
        cartItem.productId = response.productId;
        cartItem.quantity = response.quantity;
        return cartItem;
    }
}
