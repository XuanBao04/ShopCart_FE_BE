package com.shopcart.entity;

import com.shopcart.entity.enums.OrderStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "order_id", length = 50)
    public String id; 

    @Column(name = "user_id", nullable = false)
    public String userId;

    @Column(name = "total_price", nullable = false)
    public Long totalPrice; 

    @Column(name = "shipping_fee")
    public Long shippingFee;

    @Column(name = "coupon_code", length = 50)
    public String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public OrderStatus status; 

    @Column(name = "created_date")
    public LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    public LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<OrderItem> orderItems = new ArrayList<>();
    
    public Order() {}
    
    public Order(String id, String userId, Long totalPrice, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
    }
}