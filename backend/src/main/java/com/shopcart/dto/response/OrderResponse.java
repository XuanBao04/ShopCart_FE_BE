package com.shopcart.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    public String id;
    public String userId;
    public List<OrderItemResponse> items;
    public String status;
    public LocalDateTime createdDate;
    public LocalDateTime lastModifiedDate;
    
    public OrderResponse() {}
    
    public OrderResponse(String id, String userId, List<OrderItemResponse> items, String status,
                         LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.status = status;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }
}
