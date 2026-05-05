package com.shopcart.coupon.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.coupon.entity.Coupon;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends BaseRepository<Coupon, String> {
}
