package com.shopcart.order.service;

import com.shopcart.order.mapper.OrderMapper;
import com.shopcart.order.repository.OrderRepository;
import com.shopcart.cart.service.ICartService;
import com.shopcart.coupon.service.ICouponService;
import com.shopcart.inventory.service.IInventoryService;
import com.shopcart.product.service.IProductService;
import com.shopcart.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public abstract class BaseOrderServiceTest {

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
