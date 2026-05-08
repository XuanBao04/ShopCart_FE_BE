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
    @DisplayName("Nên cập nhật tổng số lượng tồn kho thành công (Ghi đè số lượng)")
    void testUpdateStock_Success() {
        String productId = "PROD-1";
        // Giả sử kho đang có 100, dành riêng 20 -> khả dụng 80
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 20);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        // Cập nhật tổng kho thành 150 (Khả dụng mới = 150 - 20 = 130)
        inventoryService.updateStock(productId, 150);

        assertEquals(150, inventory.getQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    @DisplayName("Nên thực hiện giữ hàng (reserve) thành công qua Atomic SQL")
    void testReserveStock_Success() {
        String productId = "PROD-1";
        int quantityToReserve = 30;

        when(inventoryRepository.reserveStockAtomic(productId, quantityToReserve)).thenReturn(1);

        inventoryService.reserveStock(productId, quantityToReserve);

        verify(inventoryRepository, times(1)).reserveStockAtomic(productId, quantityToReserve);
    }

    @Test
    @DisplayName("Nên xác nhận bán hàng (confirm) thành công")
    void testConfirmStock_Success() {
        String productId = "PROD-1";
        // total: 100, reserved: 30, sold: 10
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 30);
        inventory.setSoldQuantity(10);
        
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.confirmStock(productId, 30);

        // Logic confirm: reserved -= 30 (0), total -= 30 (70), sold += 30 (40)
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(70, inventory.getQuantity());
        assertEquals(40, inventory.getSoldQuantity());
        verify(inventoryRepository, times(1)).save(inventory);
    }
}
