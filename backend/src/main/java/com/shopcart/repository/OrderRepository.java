package com.shopcart.repository;

import com.shopcart.entity.Order;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends BaseRepository<Order, String> {
    List<Order> findByUserId(String userId);
}
