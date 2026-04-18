package com.shopcart.repository;

import com.shopcart.entity.Product;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends BaseRepository<Product, String> {
    // Chỉ chứa các query đặc thù riêng của Product (nếu có)
}