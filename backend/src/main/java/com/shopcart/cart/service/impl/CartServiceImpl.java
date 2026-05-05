package com.shopcart.cart.service.impl;

import com.shopcart.constant.MessageConstant;

import com.shopcart.cart.dto.request.CartItemRequest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.cart.mapper.CartMapper;
import com.shopcart.cart.repository.CartRepository;
import com.shopcart.cart.service.ICartService;
import com.shopcart.inventory.service.IInventoryService;
import com.shopcart.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Service xử lý các thao tác liên quan đến giỏ hàng (Cart).
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final IProductService productService;
    private final IInventoryService inventoryService;

    @Override
    public CartResponse getCart(String userId) {
        return buildCartResponse(userId);
    }
  
    @Override
    @Transactional
    public CartResponse addToCart(String userId, CartItemRequest request) {
        // Kiểm tra sản phẩm có tồn tại không
        productService.getProductById(request.getProductId());

        // Lấy cart item hoặc tạo mới với quantity = 0 nếu chưa tồn tại
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, request.getProductId())
                .orElseGet(() -> CartItem.builder()
                        .userId(userId)
                        .productId(request.getProductId())
                        .quantity(0)
                        .build());

        // Tính toán số lượng mới
        int newQuantity = cartItem.getQuantity() + request.getQuantity();

        // Kiểm tra tồn kho một lần duy nhất cho số lượng mới
        if (!inventoryService.hasEnoughStock(request.getProductId(), newQuantity)) {
            throw new BusinessLogicException(MessageConstant.Inventory.INSUFFICIENT_STOCK + request.getProductId());
        }

        // Cập nhật số lượng và thời gian tạo để áp dụng cho cả trường hợp mới và cộng dồn
        cartItem.setQuantity(newQuantity);
        cartItem.setCreatedAt(LocalDateTime.now());

        cartRepository.save(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(String userId, Long cartItemId) {
        // Tìm và xác thực cart item thuộc về user
       CartItem cartItem = findCartItemByUser(userId, cartItemId);

        cartRepository.delete(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(String userId, Long cartItemId, Integer quantity) {
        // Tìm và xác thực cart item thuộc về user
        CartItem cartItem = findCartItemByUser(userId, cartItemId);

        if (!inventoryService.hasEnoughStock(cartItem.getProductId(), quantity)) {
            throw new BusinessLogicException(MessageConstant.Inventory.INSUFFICIENT_STOCK + cartItem.getProductId());
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }

    // Private Helper Methods 

    /*
     * Tìm CartItem theo ID và kiểm tra quyền sở hữu của user.
     * Ném exception nếu không tìm thấy hoặc không thuộc về user.
     */
    private CartItem findCartItemByUser(String userId, Long cartItemId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageConstant.Cart.NOT_FOUND + cartItemId));

        if (!cartItem.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(
                    MessageConstant.Cart.WRONG_USER + userId);
        }

        return cartItem;
    }

    /**
     * Lấy toàn bộ cart items của user và build thành CartResponse.
     */
    private CartResponse buildCartResponse(String userId) {
        return cartMapper.toCartResponse(userId, cartRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}

