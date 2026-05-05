package com.shopcart.product.service.impl;

import com.shopcart.product.entity.Product;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.product.repository.ProductRepository;
import com.shopcart.inventory.service.IInventoryService;
import com.shopcart.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service implementation for Product operations
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final IInventoryService inventoryService;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    @Override
    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findAll().stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    @Override
    public Integer getAvailableStock(String productId) {
        // Verify product exists
        getProductById(productId);
        return inventoryService.getStock(productId);
    }

    @Override
    public boolean isProductAvailable(String productId) {
        return inventoryService.hasEnoughStock(productId, 1);
    }

    @Override
    public Product getProductWithInventory(String productId) {
        // TODO: Implement logic
        return getProductById(productId);
    }

    @Override
    public long deleteAllProducts() {
        long count = productRepository.count();
        productRepository.deleteAll();
        return count;
    }
}
