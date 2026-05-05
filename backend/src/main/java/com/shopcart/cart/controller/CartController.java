package com.shopcart.cart.controller;

import com.shopcart.cart.dto.request.CartItemRequest;
import com.shopcart.cart.dto.request.UpdateQuantityRequest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * REST Controller for Cart operations
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable String userId,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable String userId,
            @PathVariable Long cartItemId) {
        CartResponse response = cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable String userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        CartResponse response = cartService.updateQuantity(userId, cartItemId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
