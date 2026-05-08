package com.shopcart.inventory.service.impl;

import com.shopcart.constant.MessageConstant;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.inventory.repository.InventoryRepository;
import com.shopcart.inventory.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Inventory management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

    private final InventoryRepository inventoryRepository;

    private int getSafeReservedQuantity(Inventory inventory) {
        return inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
    }

    private int getSafeSoldQuantity(Inventory inventory) {
        return inventory.getSoldQuantity() == null ? 0 : inventory.getSoldQuantity();
    }

    // ======================== Inventory Retrieval ========================

    @Override
    public Integer getStock(String productId) {
        validateProductId(productId);
        Inventory inventory = findInventoryOrThrow(productId);
        return calculateAvailableStock(inventory);
    }

    @Override
    public boolean hasEnoughStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
        if (inventory == null) {
            return false;
        }
        return calculateAvailableStock(inventory) >= quantity;
    }

    // ======================== Stock Operations (Transactional) ========================

    @Override
    @Transactional
    public void updateStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Inventory inventory = findInventoryOrThrow(productId);
        
        int newQuantity = inventory.getQuantity() + quantity;
        int newAvailable = newQuantity - getSafeReservedQuantity(inventory);
        if (newAvailable < 0) {
            throw new BusinessLogicException(MessageConstant.Inventory.INSUFFICIENT_STOCK + productId);
        }
        
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void reserveStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Inventory inventory = findInventoryWithLockOrThrow(productId);
        
        int availableStock = inventory.getQuantity() - getSafeReservedQuantity(inventory);
        if (availableStock < quantity) {
            throw new BusinessLogicException(MessageConstant.Inventory.INSUFFICIENT_STOCK_RESERVE + productId);
        }
        
        inventory.setReservedQuantity(getSafeReservedQuantity(inventory) + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void releaseStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Inventory inventory = findInventoryOrThrow(productId);
        
        int newReserved = getSafeReservedQuantity(inventory) - quantity;
        if (newReserved < 0) newReserved = 0;
        inventory.setReservedQuantity(newReserved);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void confirmStock(String productId, Integer quantity) {
        validateProductId(productId);
        validateQuantity(quantity);
        
        Inventory inventory = findInventoryOrThrow(productId);
        
        int newReserved = getSafeReservedQuantity(inventory) - quantity;
        if (newReserved < 0) newReserved = 0;
        
        inventory.setReservedQuantity(newReserved);
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setSoldQuantity(getSafeSoldQuantity(inventory) + quantity);
        
        inventoryRepository.save(inventory);
    }

    // ======================== Private Helper Methods ========================

    /**
     * Find inventory or throw ResourceNotFoundException
     */
    private Inventory findInventoryOrThrow(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    MessageConstant.Inventory.NOT_FOUND + productId));
    }

    /**
     * Find inventory with Pessimistic Lock or throw ResourceNotFoundException
     */
    private Inventory findInventoryWithLockOrThrow(String productId) {
        return inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    MessageConstant.Inventory.NOT_FOUND + productId));
    }

    /**
     * Calculate available stock = total quantity - reserved
     */
    private int calculateAvailableStock(Inventory inventory) {
        return inventory.getQuantity() - getSafeReservedQuantity(inventory);
    }

    /**
     * Get reserved quantity with null-safety
     */
    private int getReservedQuantity(Inventory inventory) {
        return inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
    }

    /**
     * Get sold quantity with null-safety
     */
    private int getSoldQuantity(Inventory inventory) {
        return inventory.getSoldQuantity() != null ? inventory.getSoldQuantity() : 0;
    }

    /**
     * Validate product ID is not null or empty
     */
    private void validateProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            throw new BusinessLogicException(MessageConstant.Product.ID_REQUIRED);
        }
    }

    /**
     * Validate quantity is positive
     */
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessLogicException(MessageConstant.Inventory.POSITIVE_QUANTITY);
        }
    }
}
