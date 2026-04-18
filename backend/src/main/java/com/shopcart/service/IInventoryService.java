package com.shopcart.service;

/**
 * Service interface for Inventory management
 */
public interface IInventoryService {

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
}
