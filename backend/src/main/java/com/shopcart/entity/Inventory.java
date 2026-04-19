package com.shopcart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "product_id", unique = true, nullable = false)
    public String productId;

    @Column(nullable = false)
    public Integer quantity;
    
    public Inventory() {}
    
    public Inventory(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}