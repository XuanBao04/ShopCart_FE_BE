package com.shopcart.inventory.factory;

import com.shopcart.inventory.entity.Inventory;

public final class InventoryTestFactory {

    private InventoryTestFactory() {
    }

    public static Inventory inventory(String productId, int quantity, int reserved) {
        return Inventory.builder()
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(reserved)
                .soldQuantity(0)
                .build();
    }
}
