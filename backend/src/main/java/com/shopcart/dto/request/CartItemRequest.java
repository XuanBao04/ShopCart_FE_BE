package com.shopcart.dto.request;

public class CartItemRequest {
    public String productId;
    public Integer quantity;
    
    public CartItemRequest() {}
    
    public CartItemRequest(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}