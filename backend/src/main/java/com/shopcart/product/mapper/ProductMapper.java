package com.shopcart.product.mapper;

import com.shopcart.product.entity.Product;
import com.shopcart.product.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  
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

   
    public Product toEntity(Product product) {
        return product;
    }
}
