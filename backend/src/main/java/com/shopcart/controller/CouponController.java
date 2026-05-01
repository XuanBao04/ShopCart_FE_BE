package com.shopcart.controller;

import com.shopcart.dto.request.CreateCouponRequest;
import com.shopcart.dto.request.UpdateCouponRequest;
import com.shopcart.entity.Coupon;
import com.shopcart.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final ICouponService couponService;

    /**
     * Get all coupons (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get coupon by code (public endpoint)
     */
    @GetMapping("/{code}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable String code) {
        Coupon coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    /**
     * Check if coupon is valid
     */
    @GetMapping("/{code}/validate")
    public ResponseEntity<Boolean> validateCoupon(@PathVariable String code) {
        boolean isValid = couponService.isCouponValid(code);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Calculate discount for given coupon and order amount
     */
    @GetMapping("/{code}/discount")
    public ResponseEntity<Long> getDiscount(
            @PathVariable String code,
            @RequestParam Long orderAmount) {
        try {
            Long discount = couponService.calculateDiscount(code, orderAmount);
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create new coupon (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody CreateCouponRequest request) {
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountPercent(request.getDiscountPercent())
                .active(request.getActive() != null ? request.getActive() : true)
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : 0L)
                .expiryDate(request.getExpiryDate())
                .build();
        Coupon created = couponService.createCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update coupon (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{code}")
    public ResponseEntity<Coupon> updateCoupon(
            @PathVariable String code,
            @RequestBody UpdateCouponRequest request) {
        Coupon coupon = Coupon.builder()
                .discountPercent(request.getDiscountPercent())
                .active(request.getActive())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .expiryDate(request.getExpiryDate())
                .build();
        Coupon updated = couponService.updateCoupon(code, coupon);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete coupon (Admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String code) {
        couponService.deleteCoupon(code);
        return ResponseEntity.noContent().build();
    }
}
