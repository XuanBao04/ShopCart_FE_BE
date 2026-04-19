package com.shopcart.service.service.impl;

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

    @Override
    public Integer getStock(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        return inventory.getQuantity();
    }

    @Override
    public void updateStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        int newQuantity = inventory.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new BusinessLogicException("Insufficient stock for product: " + productId);
        }
        
        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void reserveStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        if (inventory.getQuantity() < quantity) {
            throw new BusinessLogicException("Insufficient stock to reserve for product: " + productId);
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void releaseStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productId));
        
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public boolean hasEnoughStock(String productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId).orElse(null);
        return inventory != null && inventory.getQuantity() >= quantity;
    }
}
