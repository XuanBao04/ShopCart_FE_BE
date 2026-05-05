package com.shopcart.product.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.product.entity.Product;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends BaseRepository<Product, String> {
    
}