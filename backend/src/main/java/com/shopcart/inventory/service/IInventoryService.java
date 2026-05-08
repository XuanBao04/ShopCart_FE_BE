package com.shopcart.inventory.service;

import com.shopcart.inventory.dto.response.InventoryResponse;

/**
 * Service interface for Inventory management
 */
public interface IInventoryService {

    /**
     * Get full inventory details for a product
     * @param productId the product ID
     * @return inventory details
     */
    InventoryResponse getInventoryDetails(String productId);

    /**
     * Get available stock for a product
     * @param productId the product ID
     * @return available quantity
     */
    Integer getStock(String productId);

    /**
     * Update product stock
     * @param productId the product ID
     * @param quantity quantity to add/subtract
     */
    void updateStock(String productId, Integer quantity);

    /**
     * Reserve stock for an order
     * @param productId the product ID
     * @param quantity quantity to reserve
     */
    void reserveStock(String productId, Integer quantity);

    /**
     * Release reserved stock
     * @param productId the product ID
     * @param quantity quantity to release
     */
    void releaseStock(String productId, Integer quantity);

    /**
     * Check if enough stock is available
     * @param productId the product ID
     * @param quantity required quantity
     * @return true if available, false otherwise
     */
    boolean hasEnoughStock(String productId, Integer quantity);

    /**
     * Confirm sold stock for an order (chuyển từ reserved sang sold)
     * @param productId the product ID
     * @param quantity quantity to confirm as sold
     */
    void confirmStock(String productId, Integer quantity);

    /**
     * Ship stock from warehouse (when order status changes to SHIPPED)
     * Logic: REDUCE quantity, REDUCE reservedQuantity, INCREASE soldQuantity
     * Result: availableStock remains unchanged
     * 
     * @param productId the product ID
     * @param quantity quantity to ship
     */
    void shipStock(String productId, Integer quantity);
}
