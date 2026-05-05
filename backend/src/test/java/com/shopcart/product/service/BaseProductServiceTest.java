package com.shopcart.product.service;

import com.shopcart.product.repository.ProductRepository;
import com.shopcart.inventory.service.IInventoryService;
import com.shopcart.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseProductServiceTest {

    @Mock
    protected ProductRepository productRepository;

    @Mock
    protected IInventoryService inventoryService;

    @InjectMocks
    protected ProductServiceImpl productService;
}
