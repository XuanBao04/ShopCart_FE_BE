package com.shopcart.service.impl;

import com.shopcart.entity.Coupon;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.repository.CouponRepository;
import com.shopcart.service.ICouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {
    
    private final CouponRepository couponRepository;
    private static final ZoneId VN_TIMEZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_DISCOUNT_PERCENT = 100;
    private static final int MIN_DISCOUNT_PERCENT = 1;

    // ======================== Coupon Validation ========================

    @Override
    public Coupon validateAndGetCoupon(String couponCode, Long orderAmount) {
        if (isBlankCouponCode(couponCode)) {
            return null;  // No coupon provided
        }

        validateOrderAmount(orderAmount);

        Coupon coupon = couponRepository.findById(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponCode));

        validateCouponState(coupon);
        validateMinimumOrderAmount(coupon, orderAmount);

        return coupon;
    }

    @Override
    public boolean isCouponValid(String couponCode) {
        if (isBlankCouponCode(couponCode)) {
            return false;
        }

        try {
            Coupon coupon = couponRepository.findById(couponCode).orElse(null);

            if (coupon == null) {
                return false;
            }

            return isCouponActive(coupon) && !isCouponExpired(coupon);
        } catch (Exception e) {
            log.warn("Error validating coupon: {}", couponCode, e);
            return false;
        }
    }

    // ======================== Discount Calculation ========================

    @Override
    public Long calculateDiscount(String couponCode, Long orderAmount) {
        Coupon coupon = validateAndGetCoupon(couponCode, orderAmount);
        if (coupon == null) {
            return 0L;
        }

        long discountAmount = (orderAmount * coupon.getDiscountPercent()) / 100;
        
        // Cap discount at order amount to prevent negative final price
        return Math.min(discountAmount, orderAmount);
    }

    // ======================== CRUD Operations ========================

    @Override
    public Coupon getCouponByCode(String couponCode) {
        validateCouponCodeNotBlank(couponCode);
        
        return couponRepository.findById(couponCode)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponCode));
    }

    @Override
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon cannot be null");
        validateCouponCodeNotBlank(coupon.getCode());

        validateDiscountPercent(coupon.getDiscountPercent());

        // Set defaults
        if (coupon.getActive() == null) {
            coupon.setActive(true);
        }

        if (coupon.getMinimumOrderAmount() == null) {
            coupon.setMinimumOrderAmount(0L);
        }

        LocalDateTime now = getCurrentTime();
        coupon.setCreatedAt(now);
        coupon.setUpdatedAt(now);

        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    @Transactional
    public Coupon updateCoupon(String code, Coupon coupon) {
        Objects.requireNonNull(coupon, "Coupon update data cannot be null");
        validateCouponCodeNotBlank(code);

        Coupon existing = couponRepository.findById(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));

        // Update discount percent
        if (coupon.getDiscountPercent() != null) {
            validateDiscountPercent(coupon.getDiscountPercent());
            existing.setDiscountPercent(coupon.getDiscountPercent());
        }

        // Update active status
        if (coupon.getActive() != null) {
            existing.setActive(coupon.getActive());
        }

        // Update minimum order amount
        if (coupon.getMinimumOrderAmount() != null) {
            validateMinimumOrderAmount(coupon.getMinimumOrderAmount());
            existing.setMinimumOrderAmount(coupon.getMinimumOrderAmount());
        }

        // Update expiry date
        if (coupon.getExpiryDate() != null) {
            validateExpiryDate(coupon.getExpiryDate());
            existing.setExpiryDate(coupon.getExpiryDate());
        }

        existing.setUpdatedAt(getCurrentTime());
        return couponRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteCoupon(String code) {
        validateCouponCodeNotBlank(code);

        if (!couponRepository.existsById(code)) {
            throw new ResourceNotFoundException("Coupon not found: " + code);
        }

        couponRepository.deleteById(code);
        log.info("Deleted coupon: {}", code);
    }

    // ======================== Private Validation Methods ========================

    /**
     * Validate coupon code is not null or empty
     */
    private void validateCouponCodeNotBlank(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new BusinessLogicException("Coupon code cannot be null or empty");
        }
    }

    /**
     * Check if coupon code is blank
     */
    private boolean isBlankCouponCode(String couponCode) {
        return couponCode == null || couponCode.trim().isEmpty();
    }

    /**
     * Validate discount percent is within valid range
     */
    private void validateDiscountPercent(Integer percent) {
        if (percent == null || percent < MIN_DISCOUNT_PERCENT || percent > MAX_DISCOUNT_PERCENT) {
            throw new BusinessLogicException(
                String.format("Discount percent must be between %d and %d", MIN_DISCOUNT_PERCENT, MAX_DISCOUNT_PERCENT)
            );
        }
    }

    /**
     * Validate order amount is not null and positive
     */
    private void validateOrderAmount(Long orderAmount) {
        if (orderAmount == null || orderAmount <= 0) {
            throw new BusinessLogicException("Order amount must be positive");
        }
    }

    /**
     * Validate minimum order amount is not negative
     */
    private void validateMinimumOrderAmount(Long minAmount) {
        if (minAmount != null && minAmount < 0) {
            throw new BusinessLogicException("Minimum order amount cannot be negative");
        }
    }

    /**
     * Validate coupon meets minimum order amount requirement
     */
    private void validateMinimumOrderAmount(Coupon coupon, Long orderAmount) {
        if (orderAmount < coupon.getMinimumOrderAmount()) {
            throw new BusinessLogicException(
                String.format("Minimum order amount %d required for this coupon. Current: %d",
                    coupon.getMinimumOrderAmount(), orderAmount)
            );
        }
    }

    /**
     * Validate coupon state (active and not expired)
     */
    private void validateCouponState(Coupon coupon) {
        if (!isCouponActive(coupon)) {
            throw new BusinessLogicException("Coupon is no longer active: " + coupon.getCode());
        }

        if (isCouponExpired(coupon)) {
            throw new BusinessLogicException("Coupon has expired: " + coupon.getCode());
        }
    }

    /**
     * Check if coupon is active
     */
    private boolean isCouponActive(Coupon coupon) {
        return coupon.getActive() != null && coupon.getActive();
    }

    /**
     * Check if coupon is expired
     */
    private boolean isCouponExpired(Coupon coupon) {
        if (coupon.getExpiryDate() == null) {
            return false;
        }
        return getCurrentTime().isAfter(coupon.getExpiryDate());
    }

    /**
     * Validate expiry date is not in the past
     */
    private void validateExpiryDate(LocalDateTime expiryDate) {
        if (expiryDate != null && expiryDate.isBefore(getCurrentTime())) {
            throw new BusinessLogicException("Expiry date cannot be in the past");
        }
    }

    /**
     * Get current time in Vietnam timezone
     */
    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now(VN_TIMEZONE);
    }
}
