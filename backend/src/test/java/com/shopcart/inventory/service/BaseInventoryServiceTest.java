package com.shopcart.inventory.service;

import com.shopcart.inventory.repository.InventoryRepository;
import com.shopcart.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseInventoryServiceTest {

    @Mock
    protected InventoryRepository inventoryRepository;

    @InjectMocks
    protected InventoryServiceImpl inventoryService;
}
