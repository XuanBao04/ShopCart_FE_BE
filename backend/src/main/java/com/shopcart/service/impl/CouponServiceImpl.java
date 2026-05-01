package com.shopcart.service.impl;

import com.shopcart.entity.Coupon;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.repository.CouponRepository;
import com.shopcart.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {
    private final CouponRepository couponRepository;

    @Override
    public Coupon validateAndGetCoupon(String couponCode, Long orderAmount) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return null;  // No coupon provided, return null
        }

        Coupon coupon = couponRepository.findById(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponCode));

        // Check if coupon is active
        if (!coupon.getActive()) {
            throw new BusinessLogicException("Coupon is no longer active: " + couponCode);
        }

        // Check if coupon has expired
        if (coupon.getExpiryDate() != null) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            if (now.isAfter(coupon.getExpiryDate())) {
                throw new BusinessLogicException("Coupon has expired: " + couponCode);
            }
        }

        // Check minimum order amount
        if (orderAmount < coupon.getMinimumOrderAmount()) {
            throw new BusinessLogicException(
                    String.format("Minimum order amount %d is required to apply this coupon. Current: %d",
                            coupon.getMinimumOrderAmount(), orderAmount)
            );
        }

        return coupon;
    }

    @Override
    public Long calculateDiscount(String couponCode, Long orderAmount) {
        Coupon coupon = validateAndGetCoupon(couponCode, orderAmount);
        if (coupon == null) {
            return 0L;
        }

        // Calculate discount: (subtotal * discountPercent) / 100
        return (orderAmount * coupon.getDiscountPercent()) / 100;
    }

    @Override
    public Coupon getCouponByCode(String couponCode) {
        return couponRepository.findById(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponCode));
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        // Validate coupon
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new BusinessLogicException("Coupon code cannot be empty");
        }

        if (coupon.getDiscountPercent() == null || coupon.getDiscountPercent() <= 0 || coupon.getDiscountPercent() > 100) {
            throw new BusinessLogicException("Discount percent must be between 1 and 100");
        }

        if (coupon.getActive() == null) {
            coupon.setActive(true);
        }

        if (coupon.getMinimumOrderAmount() == null) {
            coupon.setMinimumOrderAmount(0L);
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        coupon.setCreatedAt(now);
        coupon.setUpdatedAt(now);

        return couponRepository.save(coupon);
    }

    @Override
    public boolean isCouponValid(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return false;
        }

        try {
            Coupon coupon = couponRepository.findById(couponCode)
                    .orElse(null);

            if (coupon == null || !coupon.getActive()) {
                return false;
            }

            // Check if expired
            if (coupon.getExpiryDate() != null) {
                LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                if (now.isAfter(coupon.getExpiryDate())) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon updateCoupon(String code, Coupon coupon) {
        // Verify coupon exists
        Coupon existing = couponRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));

        // Update fields
        if (coupon.getDiscountPercent() != null) {
            if (coupon.getDiscountPercent() <= 0 || coupon.getDiscountPercent() > 100) {
                throw new BusinessLogicException("Discount percent must be between 1 and 100");
            }
            existing.setDiscountPercent(coupon.getDiscountPercent());
        }

        if (coupon.getActive() != null) {
            existing.setActive(coupon.getActive());
        }

        if (coupon.getMinimumOrderAmount() != null) {
            existing.setMinimumOrderAmount(coupon.getMinimumOrderAmount());
        }

        if (coupon.getExpiryDate() != null) {
            existing.setExpiryDate(coupon.getExpiryDate());
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        existing.setUpdatedAt(now);

        return couponRepository.save(existing);
    }

    @Override
    public void deleteCoupon(String code) {
        if (!couponRepository.existsById(code)) {
            throw new ResourceNotFoundException("Coupon not found: " + code);
        }
        couponRepository.deleteById(code);
    }
}
