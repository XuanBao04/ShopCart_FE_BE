package com.shopcart.order.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.order.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends BaseRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("select distinct o from Order o left join fetch o.orderItems")
    List<Order> findAllWithItems();

    @Query("select distinct o from Order o left join fetch o.orderItems where o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") String orderId);

    @Query("""
            select distinct o
            from Order o
            left join fetch o.orderItems
            where o.userId = :userId
            order by o.createdAt desc
            """)
    List<Order> findByUserIdOrderByCreatedAtDescWithItems(@Param("userId") String userId);
}
