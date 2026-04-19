package com.shopcart.dto.response;

import java.util.List;

public class CartResponse {
    public String userId;
    public List<CartItemResponse> items;
    public Integer totalItems;
    public Long totalPrice;
    
    public CartResponse() {}
    
    public CartResponse(String userId, List<CartItemResponse> items, Integer totalItems, Long totalPrice) {
        this.userId = userId;
        this.items = items;
        this.totalItems = totalItems;
        this.totalPrice = totalPrice;
    }
}