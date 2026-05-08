package com.shopcart.inventory.repository;
import com.shopcart.common.repository.BaseRepository;

import com.shopcart.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends BaseRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);

    List<Inventory> findByProductIdIn(Collection<String> productIds);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(String productId);
}
