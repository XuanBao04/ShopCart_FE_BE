package com.shopcart.dto.response;

public class CartItemResponse {
    public Long id;
    public String productId;
    public Integer quantity;
    
    public CartItemResponse() {}
    
    public CartItemResponse(Long id, String productId, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }
}
