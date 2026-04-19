package com.shopcart.dto.request;

public class OrderItemRequest {
    public String productId;
    public Integer quantity;
    public Long price;
    
    public OrderItemRequest() {}
    
    public OrderItemRequest(String productId, Integer quantity, Long price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}
