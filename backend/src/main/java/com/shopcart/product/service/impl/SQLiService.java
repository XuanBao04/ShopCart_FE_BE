package com.shopcart.product.service.impl;

import com.shopcart.product.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SQLiService {

    @PersistenceContext
    private EntityManager entityManager;

    
    @SuppressWarnings("unchecked")
    public List<Product> searchVulnerable(String name) {
        String sql = "SELECT * FROM products WHERE name = '" + name + "'";
        return entityManager.createNativeQuery(sql, Product.class).getResultList();
    }

   
    @SuppressWarnings("unchecked")
    public List<Product> searchSecure(String name) {
        String sql = "SELECT * FROM products WHERE name = :name";
        return entityManager.createNativeQuery(sql, Product.class)
                .setParameter("name", name)
                .getResultList();
    }
}
