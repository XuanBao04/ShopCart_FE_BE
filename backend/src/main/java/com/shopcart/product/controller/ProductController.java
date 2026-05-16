package com.shopcart.product.controller;

import com.shopcart.product.entity.Product;
import com.shopcart.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Product operations
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable String productId) {
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productService.searchProductsByName(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<Integer> getAvailableStock(@PathVariable String productId) {
        Integer stock = productService.getAvailableStock(productId);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/{productId}/availability")
    public ResponseEntity<Boolean> isProductAvailable(@PathVariable String productId) {
        boolean available = productService.isProductAvailable(productId);
        return ResponseEntity.ok(available);
    }

    @DeleteMapping
    public ResponseEntity<Long> deleteAllProducts() {
        long deletedCount = productService.deleteAllProducts();
        return ResponseEntity.ok(deletedCount);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@jakarta.validation.Valid @RequestBody com.shopcart.product.dto.request.ProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable String productId, @jakarta.validation.Valid @RequestBody com.shopcart.product.dto.request.ProductRequest request) {
        Product product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
