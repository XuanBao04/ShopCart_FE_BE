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
import com.shopcart.product.entity.Product;
import com.shopcart.product.repository.ProductRepository;
import com.shopcart.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final ProductRepository productRepository;

    @Override
    public CartResponse getCart(String userId) {
        return buildCartResponse(userId);
    }
  
    @Override
    @Transactional
    public CartResponse addToCart(String userId, CartItemRequest request) {
        // Kiểm tra sản phẩm có tồn tại không
        productService.getProductById(request.getProductId());

        // Reserve stock trước khi cập nhật giỏ hàng (sẽ ném exception nếu không đủ hàng)
        inventoryService.reserveStock(request.getProductId(), request.getQuantity());

        // Lấy cart item hoặc tạo mới với quantity = 0 nếu chưa tồn tại
        CartItem cartItem = cartRepository
                .findByUserIdAndProductId(userId, request.getProductId())
                .orElseGet(() -> CartItem.builder()
                        .userId(userId)
                        .productId(request.getProductId())
                        .quantity(0)
                        .build());

        // Cập nhật số lượng và thời gian tạo
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        cartItem.setCreatedAt(LocalDateTime.now());

        cartRepository.save(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(String userId, Long cartItemId) {
        // Tìm và xác thực cart item thuộc về user
        CartItem cartItem = findCartItemByUser(userId, cartItemId);

        // Giải phóng kho khi xóa khỏi giỏ
        inventoryService.releaseStock(cartItem.getProductId(), cartItem.getQuantity());

        cartRepository.delete(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(String userId, Long cartItemId, Integer quantity) {
        // Tìm và xác thực cart item thuộc về user
        CartItem cartItem = findCartItemByUser(userId, cartItemId);
        
        int oldQuantity = cartItem.getQuantity();
        int diff = quantity - oldQuantity;

        // Điều chỉnh số lượng giữ trong kho
        if (diff > 0) {
            inventoryService.reserveStock(cartItem.getProductId(), diff);
        } else if (diff < 0) {
            inventoryService.releaseStock(cartItem.getProductId(), Math.abs(diff));
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
        return buildCartResponse(userId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        // Giải phóng kho cho tất cả các mặt hàng trong giỏ trước khi xóa
        List<CartItem> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (CartItem item : cartItems) {
            inventoryService.releaseStock(item.getProductId(), item.getQuantity());
        }
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
        List<CartItem> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Set<String> productIds = cartItems.stream()
            .map(CartItem::getProductId)
            .collect(Collectors.toSet());

        List<String> productIdList = new ArrayList<>(productIds);

        Map<String, Product> productsById = productRepository.findAllById(productIdList).stream()
            .collect(Collectors.toMap(Product::getId, product -> product));

        return cartMapper.toCartResponse(userId, cartItems, productsById);
    }
}

