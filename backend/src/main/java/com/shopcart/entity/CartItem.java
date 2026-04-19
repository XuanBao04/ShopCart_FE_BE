package com.shopcart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public String userId; 

    @Column(name = "product_id", nullable = false)
    public String productId;

    @Column(nullable = false)
    public Integer quantity;
    
    public CartItem() {}
    
    public CartItem(String userId, String productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }
}