package com.shopcart.inventory.repository;
import com.shopcart.common.repository.BaseRepository;
import org.springframework.data.repository.query.Param;
import com.shopcart.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends BaseRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(String productId);

    List<Inventory> findByProductIdIn(Collection<String> productIds);



    // lock pessimistic 
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.productId = :productId")
    Optional<Inventory> findByProductIdWithLock(String productId);


    // sql atomic - Reserve stock (when creating order)
    @Modifying
    @Query("""
        UPDATE Inventory i
        SET i.reservedQuantity =
            COALESCE(i.reservedQuantity, 0) + :quantity
        WHERE i.productId = :productId
        AND (
            i.quantity - COALESCE(i.reservedQuantity, 0)
        ) >= :quantity
    """)
    int reserveStockAtomic(
            @Param("productId") String productId,
            @Param("quantity") Integer quantity
    );

    /**
     * Atomic update to ship stock from warehouse
     * Logic when SHIPPED (Release goods from warehouse):
     * - REDUCE quantity (physical inventory decreases)
     * - REDUCE reservedQuantity (cancel hold status)
     * - INCREASE soldQuantity
     * Result: availableStock remains unchanged (quantity - reserved)
     * 
     * @param productId Product ID
     * @param quantity Quantity to ship
     * @return 1 if update successful, 0 if reserved quantity is insufficient
     */
    @Modifying
    @Query("""
        UPDATE Inventory i
        SET i.quantity = i.quantity - :quantity,
            i.reservedQuantity = COALESCE(i.reservedQuantity, 0) - :quantity,
            i.soldQuantity = COALESCE(i.soldQuantity, 0) + :quantity
        WHERE i.productId = :productId
        AND COALESCE(i.reservedQuantity, 0) >= :quantity
    """)
    int shipStockAtomic(
            @Param("productId") String productId,
            @Param("quantity") Integer quantity
    );
}
