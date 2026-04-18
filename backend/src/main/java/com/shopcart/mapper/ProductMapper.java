package com.shopcart.mapper;

import com.shopcart.entity.Product;
import com.shopcart.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Product entities to DTOs
 */
@Component
public class ProductMapper {

    /**
     * Convert Product entity to ProductResponse
     */
    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus() != null ? product.getStatus().toString() : null)
                .build();
    }

    /**
     * Convert Product entity to Product (identity mapping)
     */
    public Product toEntity(Product product) {
        return product;
    }
}
