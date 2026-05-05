package com.shopcart.inventory.service.get;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.inventory.factory.InventoryTestFactory;
import com.shopcart.inventory.service.BaseInventoryServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("Inventory Service — Lấy thông tin kho (Luồng thành công)")
class InventoryGetHappyPathTest extends BaseInventoryServiceTest {

    @Test
    @DisplayName("Nên lấy được số lượng tồn kho khả dụng chính xác")
    void testGetStock_Success() {
        String productId = "PROD-1";
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 20);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        Integer result = inventoryService.getStock(productId);

        assertEquals(80, result); // 100 - 20 = 80
    }

    @Test
    @DisplayName("Nên kiểm tra trạng thái đủ hàng chính xác")
    void testHasEnoughStock_Success() {
        String productId = "PROD-1";
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 20);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        boolean result = inventoryService.hasEnoughStock(productId, 50);

        assertTrue(result);
    }
}
