package com.shopcart.order.service;

import com.shopcart.mapper.OrderMapper;
import com.shopcart.repository.OrderRepository;
import com.shopcart.service.ICartService;
import com.shopcart.service.ICouponService;
import com.shopcart.service.IInventoryService;
import com.shopcart.service.IProductService;
import com.shopcart.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Shared Mockito wiring and order test fixtures for OrderServiceImpl tests.
 */
@ExtendWith(MockitoExtension.class)
abstract class BaseOrderServiceTest {

    @Mock
    protected OrderRepository orderRepository;

    @Mock
    protected OrderMapper orderMapper;

    @Mock
    protected IInventoryService inventoryService;

    @Mock
    protected IProductService productService;

    @Mock
    protected ICartService cartService;

    @Mock
    protected ICouponService couponService;

    @InjectMocks
    protected OrderServiceImpl orderService;

    protected String testUserId;
    protected String testProductId;
    protected String testOrderId;

    @BeforeEach
    void baseSetUp() {
        testUserId = "user-123";
        testProductId = "product-456";
        testOrderId = "order-789";
    }
}
