package com.shopcart.dto.request;

import java.util.List;

public class OrderRequest {
    public String userId;
    public List<OrderItemRequest> orderItems;
    
    public OrderRequest() {}
    
    public OrderRequest(String userId, List<OrderItemRequest> orderItems) {
        this.userId = userId;
        this.orderItems = orderItems;
    }
}
