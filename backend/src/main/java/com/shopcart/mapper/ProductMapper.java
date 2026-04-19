package com.shopcart.mapper;

import com.shopcart.entity.Product;
import com.shopcart.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  
    public ProductResponse toProductResponse(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductResponse(
                product.id,
                product.name,
                product.description,
                product.price,
                product.status != null ? product.status.toString() : null
        );
    }

   
    public Product toEntity(Product product) {
        return product;
    }
}
