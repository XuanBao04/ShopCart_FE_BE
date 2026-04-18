package com.shopcart.dto.response;

import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {
    private String userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private Long totalPrice;
}