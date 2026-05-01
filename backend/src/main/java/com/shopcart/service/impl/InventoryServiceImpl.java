package com.shopcart.service.impl;

import com.shopcart.entity.Inventory;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.repository.InventoryRepository;
import com.shopcart.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Inventory management
 */
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

    @Override
    public Integer getStock(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        return inventory.getQuantity() - getSafeReservedQuantity(inventory);
    }

    @Override
    public void updateStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        int newQuantity = inventory.getQuantity() + quantity;
        int newAvailable = newQuantity - getSafeReservedQuantity(inventory);
        if (newAvailable < 0) {
            throw new BusinessLogicException("Insufficient stock for product: " + productId);
        }
        
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void reserveStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        int availableStock = inventory.getQuantity() - getSafeReservedQuantity(inventory);
        if (availableStock < quantity) {
            throw new BusinessLogicException("Insufficient stock to reserve for product: " + productId);
        }
        
        inventory.setReservedQuantity(getSafeReservedQuantity(inventory) + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void releaseStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        int newReserved = getSafeReservedQuantity(inventory) - quantity;
        if (newReserved < 0) newReserved = 0;
        inventory.setReservedQuantity(newReserved);
        inventoryRepository.save(inventory);
    }

    @Override
    public boolean hasEnoughStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
        if (inventory == null) return false;
        return (inventory.getQuantity() - getSafeReservedQuantity(inventory)) >= quantity;
    }

    @Override
    public void confirmStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        int newReserved = getSafeReservedQuantity(inventory) - quantity;
        if (newReserved < 0) newReserved = 0;
        
        inventory.setReservedQuantity(newReserved);
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setSoldQuantity(getSafeSoldQuantity(inventory) + quantity);
        
        inventoryRepository.save(inventory);
    }
}
