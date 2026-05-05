package com.shopcart.product.repository;

import com.shopcart.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SecurityRepository extends JpaRepository<Product, String> {

    @Query(value = "SELECT * FROM products WHERE name = '"+"?1"+"'", nativeQuery = true)
    List<Product> searchByNameVulnerable(String name);
    
    
    @Query(value = "SELECT * FROM products WHERE name = :name", nativeQuery = true)
    List<Product> searchByNameSecure(@Param("name") String name);
}
