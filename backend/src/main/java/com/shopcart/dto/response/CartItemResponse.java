package com.shopcart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long id;
    private String productId;
    private Integer quantity;
    private Long price;
    private Long totalPrice;
    private LocalDateTime createdAt;
}
