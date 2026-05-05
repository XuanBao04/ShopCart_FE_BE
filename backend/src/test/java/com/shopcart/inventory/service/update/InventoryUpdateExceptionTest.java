package com.shopcart.inventory.service.update;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.inventory.factory.InventoryTestFactory;
import com.shopcart.inventory.service.BaseInventoryServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Inventory Service — Cập nhật kho hàng (Trường hợp lỗi)")
class InventoryUpdateExceptionTest extends BaseInventoryServiceTest {

    @Test
    @DisplayName("Nên ném lỗi BusinessLogicException khi không đủ hàng để giữ")
    void testReserveStock_Insufficient() {
        String productId = "PROD-1";
        Inventory inventory = InventoryTestFactory.inventory(productId, 100, 80); // 20 available
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> inventoryService.reserveStock(productId, 30)
        );

        assertEquals("Insufficient stock to reserve for product: " + productId, exception.getMessage());
    }

    @Test
    @DisplayName("Nên ném lỗi BusinessLogicException khi số lượng không hợp lệ")
    void testUpdateStock_InvalidQuantity() {
        BusinessLogicException exception = assertThrows(
                BusinessLogicException.class,
                () -> inventoryService.updateStock("PROD-1", -10)
        );

        assertEquals("Quantity must be a positive integer", exception.getMessage());
    }
}
