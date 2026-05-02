package com.shopcart.repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import com.shopcart.entity.CartItem;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends BaseRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByCreatedAtDesc(String userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    void deleteByUserId(String userId);
}