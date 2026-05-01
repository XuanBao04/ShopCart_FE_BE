package com.shopcart.controller;

import com.shopcart.dto.request.UpdateStockRequest;
import com.shopcart.service.IInventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Inventory operations
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IInventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable String productId) {
        Integer stock = inventoryService.getStock(productId);
        return ResponseEntity.ok(stock);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Void> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest request) {
        inventoryService.updateStock(productId, request.getQuantity());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        inventoryService.reserveStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/release")
    public ResponseEntity<Void> releaseStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        inventoryService.releaseStock(productId, quantity);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> hasEnoughStock(
            @PathVariable String productId,
            @RequestParam Integer quantity) {
        boolean hasStock = inventoryService.hasEnoughStock(productId, quantity);
        return ResponseEntity.ok(hasStock);
    }
}
