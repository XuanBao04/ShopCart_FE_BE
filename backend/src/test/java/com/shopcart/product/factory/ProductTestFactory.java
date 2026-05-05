package com.shopcart.product.factory;

import com.shopcart.product.entity.Product;
import com.shopcart.common.enums.ProductStatus;

public final class ProductTestFactory {

    private ProductTestFactory() {
    }

    public static Product activeProduct(String id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(100000L)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    public static Product inactiveProduct(String id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(50000L)
                .status(ProductStatus.INACTIVE)
                .build();
    }
}
