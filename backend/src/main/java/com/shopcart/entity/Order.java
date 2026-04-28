package com.shopcart.entity;

import com.shopcart.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @Column(name = "order_id", length = 50)
    private String id; 

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice; 

    @Column(name = "shipping_fee")
    private Long shippingFee;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; 

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}