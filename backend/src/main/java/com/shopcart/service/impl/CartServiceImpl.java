package com.shopcart.service.impl;

import com.shopcart.dto.request.CartItemRequest;
import com.shopcart.dto.response.CartResponse;
import com.shopcart.entity.CartItem;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.mapper.CartMapper;
import com.shopcart.repository.CartRepository;
import com.shopcart.service.ICartService;
import com.shopcart.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service xử lý các thao tác liên quan đến giỏ hàng (Cart).
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final IProductService productService;

    @Override
    public CartResponse getCart(String userId) {
        return buildCartResponse(userId);
    }
  
    @Override
    public CartResponse addToCart(String userId, CartItemRequest request) {
        // Kiểm tra sản phẩm có tồn tại không
        productService.getProductById(request.getProductId());

        // Số lượng phải lớn hơn 0
        if (request.getQuantity() <= 0) {
            throw new BusinessLogicException("Số lượng phải lớn hơn 0");
        }

        // Kiểm tra sản phẩm đã có trong giỏ hàng chưa
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);

        if (cartItem != null) {
            // Đã có -> cộng dồn số lượng
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            // Chưa có -> tạo mới
            cartItem = CartItem.builder()
                    .userId(userId)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
        }

        cartRepository.save(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    public CartResponse removeFromCart(String userId, Long cartItemId) {
        // Tìm và xác thực cart item thuộc về user
        findCartItemByUser(userId, cartItemId);

        cartRepository.deleteById(cartItemId);
        return buildCartResponse(userId);
    }

    @Override
    public CartResponse updateQuantity(String userId, Long cartItemId, Integer quantity) {
        // Tìm và xác thực cart item thuộc về user
        CartItem cartItem = findCartItemByUser(userId, cartItemId);

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
        return buildCartResponse(userId);
    }

    @Override
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
                        "Không tìm thấy cart item với id: " + cartItemId));

        if (!cartItem.getUserId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Cart item không thuộc về user: " + userId);
        }

        return cartItem;
    }

    /**
     * Lấy toàn bộ cart items của user và build thành CartResponse.
     */
    private CartResponse buildCartResponse(String userId) {
        return cartMapper.toCartResponse(userId, cartRepository.findByUserId(userId));
    }
}

