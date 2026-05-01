package com.shopcart.service;

import com.shopcart.entity.Coupon;
import java.util.List;

public interface ICouponService {
    /**
     * Validate coupon code and check if applicable for order amount
     * @param couponCode coupon code to validate
     * @param orderAmount subtotal amount (before discount)
     * @return Coupon entity if valid and applicable
     * @throws BusinessLogicException if coupon is invalid or not applicable
     */
    Coupon validateAndGetCoupon(String couponCode, Long orderAmount);

    /**
     * Calculate discount amount
     * @param couponCode coupon code
     * @param orderAmount subtotal amount
     * @return discount amount in VND
     */
    Long calculateDiscount(String couponCode, Long orderAmount);

    /**
     * Get coupon by code
     * @param couponCode coupon code
     * @return Coupon entity
     */
    Coupon getCouponByCode(String couponCode);

    /**
     * Create new coupon
     * @param coupon coupon entity
     * @return saved coupon
     */
    Coupon createCoupon(Coupon coupon);

    /**
     * Check if coupon is valid and active
     * @param couponCode coupon code
     * @return true if valid
     */
    boolean isCouponValid(String couponCode);

    /**
     * Get all coupons
     * @return List of all coupons
     */
    List<Coupon> getAllCoupons();

    /**
     * Update existing coupon
     * @param code coupon code
     * @param coupon updated coupon entity
     * @return updated coupon
     */
    Coupon updateCoupon(String code, Coupon coupon);

    /**
     * Delete coupon by code
     * @param code coupon code
     */
    void deleteCoupon(String code);
}
