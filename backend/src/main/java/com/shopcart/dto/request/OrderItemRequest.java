package com.shopcart.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequest {
    private String productId;
    private Integer quantity;
    private Long price;
}
