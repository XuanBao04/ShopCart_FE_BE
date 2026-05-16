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
                .imageUrl(product.getImageUrl())
                .build();
    }

   
    public Product toEntity(Product product) {
        return product;
    }

    public Product toEntity(com.shopcart.product.dto.request.ProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .id(request.getId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(request.getStatus() != null ? com.shopcart.common.enums.ProductStatus.valueOf(request.getStatus()) : null)
                .imageUrl(request.getImageUrl())
                .build();
    }
}
