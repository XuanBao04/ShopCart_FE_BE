package com.shopcart.inventory.mapper;

import com.shopcart.inventory.dto.response.InventoryResponse;
import com.shopcart.inventory.entity.Inventory;

public class InventoryMapper {
    public static InventoryResponse toResponse(Inventory inventory) {
        if (inventory == null) return null;
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .soldQuantity(inventory.getSoldQuantity())
                .build();
    }
    public static Inventory toEntity(InventoryResponse response) {
        if (response == null) return null;
        return Inventory.builder()
                .id(response.getId())
                .productId(response.getProductId())
                .quantity(response.getQuantity())
                .reservedQuantity(response.getReservedQuantity())
                .soldQuantity(response.getSoldQuantity())
                .build();
    }
}
