package com.shopcart.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private String productId;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
    private Integer availableQuantity;
}
