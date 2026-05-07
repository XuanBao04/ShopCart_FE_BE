package com.shopcart.cart.service;

import com.shopcart.cart.dto.request.CartItemRequest;
import com.shopcart.cart.dto.response.CartResponse;
import com.shopcart.cart.entity.CartItem;
import com.shopcart.cart.mapper.CartMapper;
import com.shopcart.cart.repository.CartRepository;
import com.shopcart.inventory.service.IInventoryService;
import com.shopcart.product.entity.Product;
import com.shopcart.product.repository.ProductRepository;
import com.shopcart.product.service.IProductService;
import com.shopcart.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Shared Mockito wiring and cart test fixtures for CartServiceImpl tests.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseCartServiceTest {

    @Mock
    protected CartRepository cartRepository;

    @Mock
    protected CartMapper cartMapper;

    @Mock
    protected IProductService productService;

    @Mock
    protected IInventoryService inventoryService;

    @Mock
    protected ProductRepository productRepository;

    @InjectMocks
    protected CartServiceImpl cartService;

    protected String userId;
    protected CartItemRequest request;

    @BeforeEach
    void baseSetUp() {
        userId = "user-1";
        request = CartItemRequest.builder()
                .productId("PROD-001")
                .quantity(2)
                .build();
    }

    protected void mockBuildCartResponseHelper(List<CartItem> mockCartItems) {
        when(cartRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(mockCartItems);
        
        Map<String, Product> productsById = new HashMap<>();
        for (CartItem item : mockCartItems) {
            Product product = Product.builder()
                    .id(item.getProductId())
                    .price(100_000L)
                    .build();
            productsById.put(item.getProductId(), product);
        }
        
        when(productRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(productsById.values().stream().toList());
        when(cartMapper.toCartResponse(userId, mockCartItems, productsById))
                .thenReturn(new CartResponse());
    }
}
