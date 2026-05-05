package com.shopcart.inventory.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.inventory.entity.Inventory;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryRepository extends BaseRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);
}
