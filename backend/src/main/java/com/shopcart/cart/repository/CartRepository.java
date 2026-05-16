package com.shopcart.cart.repository;
import com.shopcart.common.repository.BaseRepository;
import com.shopcart.cart.entity.CartItem;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends BaseRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM CartItem c WHERE c.userId = :userId")
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(String userId);
}