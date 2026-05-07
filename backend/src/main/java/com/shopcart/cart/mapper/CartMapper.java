package com.shopcart.cart.mapper;

import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.dto.response.CartItemResponse;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.constant.MessageConstant;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class CartMapper {

   
    public CartResponse toCartResponse(String userId, List<CartItem> items, Map<String, Product> productsById) {
        if (items == null) {
            items = java.util.Collections.emptyList();
        }

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> toCartItemResponse(item, productsById))
                .collect(Collectors.toList());

        Long totalPrice = itemResponses.stream()
                .mapToLong(CartItemResponse::getTotalPrice)
                .sum();

        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalItems(items.size())
                .totalPrice(totalPrice)
                .build();
    }

    
    public CartItemResponse toCartItemResponse(CartItem item, Map<String, Product> productsById) {
        Product product = productsById.get(item.getProductId());
        if (product == null) {
            throw new ResourceNotFoundException(MessageConstant.Product.NOT_FOUND + item.getProductId());
        }

        Long price = product.getPrice();
        Long totalPrice = price * item.getQuantity();

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .price(price)
                .totalPrice(totalPrice)
                .createdAt(item.getCreatedAt())
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
