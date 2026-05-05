package com.shopcart.inventory.service.update;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.inventory.factory.InventoryTestFactory;
import com.shopcart.inventory.service.BaseInventoryServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Inventory Service — Cập nhật kho hàng (Luồng thành công)")
class InventoryUpdateHappyPathTest extends BaseInventoryServiceTest {

    @Test
    @DisplayName("Nên cập nhật tổng số lượng tồn kho thành công")
    void testUpdateStock_Success() {
        String productId = "PROD-1";
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 20);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.updateStock(productId, 50);

        assertEquals(150, inventory.getQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Nên thực hiện giữ hàng (reserve) thành công")
    void testReserveStock_Success() {
        String productId = "PROD-1";
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 20);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.reserveStock(productId, 30);

        assertEquals(50, inventory.getReservedQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Nên xác nhận bán hàng (confirm) thành công")
    void testConfirmStock_Success() {
        String productId = "PROD-1";
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 30);
        inventory.setSoldQuantity(10);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.confirmStock(productId, 30);

        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(70, inventory.getQuantity());
        assertEquals(40, inventory.getSoldQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }
}
