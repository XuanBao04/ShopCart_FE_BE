package com.shopcart.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private String productId;
    private Integer quantity;
    private Long price;
}
