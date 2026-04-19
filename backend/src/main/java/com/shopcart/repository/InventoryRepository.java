package com.shopcart.repository;

import com.shopcart.entity.Inventory;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryRepository extends BaseRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);
}
