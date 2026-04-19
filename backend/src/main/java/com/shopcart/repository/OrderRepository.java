package com.shopcart.repository;

import com.shopcart.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends BaseRepository<Order, String> {
}
