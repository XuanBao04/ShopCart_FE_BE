package com.shopcart.inventory.service.get;

import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.inventory.service.BaseInventoryServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Inventory Service — Lấy thông tin kho (Trường hợp lỗi)")
class InventoryGetExceptionTest extends BaseInventoryServiceTest {

    @Test
    @DisplayName("Nên ném lỗi ResourceNotFoundException khi không tìm thấy thông tin kho")
    void testGetStock_NotFound() {
        String productId = "PROD-1";
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> inventoryService.getStock(productId)
        );

        assertEquals("Inventory not found for product: " + productId, exception.getMessage());
    }
}
