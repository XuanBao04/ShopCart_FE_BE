package com.shopcart.service.service.impl;

import com.shopcart.entity.Product;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.repository.ProductRepository;
import com.shopcart.service.IInventoryService;
import com.shopcart.service.IProductService;
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
                .filter(p -> p.name.toLowerCase().contains(keyword.toLowerCase()))
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
}
