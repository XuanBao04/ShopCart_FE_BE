package com.shopcart.service;

import com.shopcart.dto.response.CartItemResponse;
import com.shopcart.entity.Product;
import java.util.List;

/**
 * Service interface for Product operations
 */
public interface IProductService {

    /**
     * Get all products
     * @return list of Product
     */
    List<Product> getAllProducts();

    /**
     * Get product by ID
     * @param productId the product ID
     * @return Product
     */
    Product getProductById(String productId);

    /**
     * Search products by name
     * @param keyword search keyword
     * @return list of matching products
     */
    List<Product> searchProductsByName(String keyword);

    /**
     * Get available stock for a product
     * @param productId the product ID
     * @return available quantity
     */
    Integer getAvailableStock(String productId);

    /**
     * Check if product is available
     * @param productId the product ID
     * @return true if available, false otherwise
     */
    boolean isProductAvailable(String productId);

    /**
     * Get product with inventory details
     * @param productId the product ID
     * @return Product
     */
    Product getProductWithInventory(String productId);
}
