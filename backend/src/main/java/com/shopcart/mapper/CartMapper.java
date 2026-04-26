package com.shopcart.mapper;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.entity.Product;
import com.shopcart.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CartMapper {

    private final IProductService productService;

   
    public CartResponse toCartResponse(String userId, List<CartItem> items) {
        if (items == null) {
            items = java.util.Collections.emptyList();
        }

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::toCartItemResponse)
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

    
    public CartItemResponse toCartItemResponse(CartItem item) {
        Product product = productService.getProductById(item.getProductId());
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
