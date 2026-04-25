package com.shopcart.repository;

import com.shopcart.entity.Coupon;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends BaseRepository<Coupon, String> {
}
