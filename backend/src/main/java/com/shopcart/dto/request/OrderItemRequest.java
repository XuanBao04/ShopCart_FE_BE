package com.shopcart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
    
    @Positive(message = "Price must be greater than 0")
    private Long price;
}
