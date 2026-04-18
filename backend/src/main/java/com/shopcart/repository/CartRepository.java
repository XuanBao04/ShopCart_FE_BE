package com.shopcart.repository;

import com.shopcart.entity.CartItem;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends BaseRepository<CartItem, Long> {
    /**
     * Find all cart items for a specific user
     */
    List<CartItem> findByUserId(String userId);

    /**
     * Find specific cart item by user and product ID
     */
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    /**
     * Delete all cart items for a user
     */
    void deleteByUserId(String userId);
}