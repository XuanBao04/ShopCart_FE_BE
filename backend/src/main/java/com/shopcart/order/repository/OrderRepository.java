package com.shopcart.order.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.order.entity.Order;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends BaseRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
