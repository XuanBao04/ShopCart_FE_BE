package com.shopcart.coupon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @Column(name = "coupon_code", length = 50)
    private String code;

    @Column(nullable = false)
    private Integer discountPercent;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "minimum_order_amount", nullable = false)
    @Builder.Default
    private Long minimumOrderAmount = 0L;  // VND: 0 = apply to all orders

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;  // null = no expiry

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
