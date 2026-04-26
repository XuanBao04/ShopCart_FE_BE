package com.shopcart.service;

import com.shopcart.dto.response.CartResponse;
import com.shopcart.dto.request.CartItemRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for Cart operations
 */
public interface ICartService {

    /**
     * Get cart for a specific user
     * @param userId the user ID
     * @return CartResponse containing cart items
     */
    CartResponse getCart(String userId);

    /**
     * Add item to cart
     * @param userId the user ID
     * @param request CartItemRequest with product details
     * @return updated CartResponse
     */
    CartResponse addToCart(String userId, CartItemRequest request);

    /**
     * Remove item from cart
     * @param userId the user ID
     * @param cartItemId the cart item ID to remove
     * @return updated CartResponse
     */
    CartResponse removeFromCart(String userId, Long cartItemId);

    /**
     * Update cart item quantity
     * @param userId the user ID
     * @param cartItemId the cart item ID
     * @param quantity new quantity
     * @return updated CartResponse
     */
    CartResponse updateQuantity(String userId, Long cartItemId, Integer quantity);

    /**a
     * Clear all items from cart
     * @param userId the user ID
     */
    void clearCart(String userId);
}
