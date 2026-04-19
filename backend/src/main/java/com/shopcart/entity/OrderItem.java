package com.shopcart.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    public Order order;

    @Column(name = "product_id", nullable = false)
    public String productId;

    @Column(nullable = false)
    public Integer quantity;

    @Column(nullable = false)
    public Long price;
    
    public OrderItem() {}
    
    public OrderItem(String productId, Integer quantity, Long price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
}