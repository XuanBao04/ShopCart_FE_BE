package com.shopcart.dto.response;

public class OrderItemResponse {
    public Long id;
    public String productId;
    public Integer quantity;
    public Long price;
    
    public OrderItemResponse() {}
    
    public OrderItemResponse(Long id, String productId, Integer quantity, Long price) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}
