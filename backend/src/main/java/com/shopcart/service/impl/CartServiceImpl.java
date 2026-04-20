package com.shopcart.service.impl;

import com.shopcart.dto.request.CartItemRequest;
import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.mapper.CartMapper;
import com.shopcart.repository.CartRepository;
import com.shopcart.service.ICartService;
import com.shopcart.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service implementation for Cart operations
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final IProductService productService;

    @Override
    public CartResponse getCart(String userId) {
        List<CartItem> items = cartRepository.findByUserId(userId);
        return cartMapper.toCartResponse(userId, items);
    }

    @Override
    public CartResponse addToCart(String userId, CartItemRequest request) {
        // Validate product exists
        productService.getProductById(request.getProductId());
        
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);
        
        if (cartItem != null) {
            // Update existing item
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            // Create new item
            cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
        }
        
        cartRepository.save(cartItem);
        List<CartItem> items = cartRepository.findByUserId(userId);
        return cartMapper.toCartResponse(userId, items);
    }

    @Override
    public CartResponse removeFromCart(String userId, Long cartItemId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        
        if (!cartItem.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found for user: " + userId);
        }
        
        cartRepository.deleteById(cartItemId);
        List<CartItem> items = cartRepository.findByUserId(userId);
        return cartMapper.toCartResponse(userId, items);
    }

    @Override
    public CartResponse updateCartItem(String userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        
        if (!cartItem.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found for user: " + userId);
        }
        
        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
        List<CartItem> items = cartRepository.findByUserId(userId);
        return cartMapper.toCartResponse(userId, items);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
