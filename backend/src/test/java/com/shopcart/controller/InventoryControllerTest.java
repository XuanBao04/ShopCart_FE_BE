package com.shopcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcart.dto.request.UpdateStockRequest;
import com.shopcart.service.IInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for Inventory endpoints using MockMvc
 * 
 * Demonstrates:
 * - PATCH endpoint with @RequestBody JSON payload
 * - Request validation using @Valid annotation
 * - Proper HTTP status codes (204 No Content)
 * - JSON content-type handling
 */
@Slf4j
@WebMvcTest(InventoryController.class)
@DisplayName("InventoryController Tests")
@WithMockUser(roles = "USER")
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IInventoryService inventoryService;

    private static final String PRODUCT_ID = "PROD-001";
    private static final String API_PATH = "/api/inventory";

    // ============ PATCH /api/inventory/{productId} - Update Stock ============

    @Test
    @DisplayName("Should update stock with valid request body")
    void testUpdateStockSuccess() throws Exception {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(10)
                .build();

        // Act & Assert
        mockMvc.perform(patch(API_PATH + "/{productId}", PRODUCT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        // Verify service was called with correct parameters
        verify(inventoryService, times(1)).updateStock(PRODUCT_ID, 10);
        log.info("✓ Successfully updated stock for product: {}", PRODUCT_ID);
    }

    @Test
    @DisplayName("Should return error when quantity is zero")
    void testUpdateStockWithZeroQuantity() throws Exception {
        // Arrange - Violates @Min(1)
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(0)
                .build();

        // Act & Assert
        mockMvc.perform(patch(API_PATH + "/{productId}", PRODUCT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        // Verify service was NOT called
        verify(inventoryService, never()).updateStock(anyString(), anyInt());
        log.info("✓ Validation rejected zero quantity");
    }

    @Test
    @DisplayName("Should return error when quantity is negative")
    void testUpdateStockWithNegativeQuantity() throws Exception {
        // Arrange - Violates @Min(1)
        UpdateStockRequest request = UpdateStockRequest.builder()
                .quantity(-5)
                .build();

        // Act & Assert
        mockMvc.perform(patch(API_PATH + "/{productId}", PRODUCT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        // Verify service was NOT called
        verify(inventoryService, never()).updateStock(anyString(), anyInt());
        log.info("✓ Validation rejected negative quantity");
    }

    @Test
    @DisplayName("Should return error when quantity is null")
    void testUpdateStockWithNullQuantity() throws Exception {
        // Arrange - JSON without quantity field
        String jsonContent = "{}";

        // Act & Assert
        mockMvc.perform(patch(API_PATH + "/{productId}", PRODUCT_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().is5xxServerError());

        // Verify service was NOT called
        verify(inventoryService, never()).updateStock(anyString(), anyInt());
        log.info("✓ Validation rejected null quantity");
    }

    @Test
    @DisplayName("Should update stock with different valid quantities")
    void testUpdateStockWithVariousQuantities() throws Exception {
        // Test with different valid quantities
        int[] validQuantities = {1, 5, 100, 1000};

        for (int quantity : validQuantities) {
            // Arrange
            UpdateStockRequest request = UpdateStockRequest.builder()
                    .quantity(quantity)
                    .build();

            // Act & Assert
            mockMvc.perform(patch(API_PATH + "/{productId}", PRODUCT_ID)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            verify(inventoryService).updateStock(PRODUCT_ID, quantity);
        }

        log.info("✓ All valid quantities processed successfully");
    }

    // ============ GET /api/inventory/{productId} - Get Stock ============

    @Test
    @DisplayName("Should get stock successfully")
    void testGetStockSuccess() throws Exception {
        // Arrange
        when(inventoryService.getStock(PRODUCT_ID)).thenReturn(50);

        // Act & Assert
        mockMvc.perform(get(API_PATH + "/{productId}", PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(50));

        verify(inventoryService, times(1)).getStock(PRODUCT_ID);
        log.info("✓ Successfully retrieved stock");
    }

    @Test
    @DisplayName("Should return zero stock")
    void testGetStockZero() throws Exception {
        // Arrange
        when(inventoryService.getStock(PRODUCT_ID)).thenReturn(0);

        // Act & Assert
        mockMvc.perform(get(API_PATH + "/{productId}", PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));

        log.info("✓ Successfully retrieved zero stock");
    }

    // ============ POST /api/inventory/{productId}/reserve - Reserve Stock ============

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserveStockSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post(API_PATH + "/{productId}/reserve", PRODUCT_ID)
                .with(csrf())
                .param("quantity", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(inventoryService, times(1)).reserveStock(PRODUCT_ID, 5);
        log.info("✓ Successfully reserved stock");
    }

    // ============ POST /api/inventory/{productId}/release - Release Stock ============

    @Test
    @DisplayName("Should release stock successfully")
    void testReleaseStockSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post(API_PATH + "/{productId}/release", PRODUCT_ID)
                .with(csrf())
                .param("quantity", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(inventoryService, times(1)).releaseStock(PRODUCT_ID, 3);
        log.info("✓ Successfully released stock");
    }

    // ============ GET /api/inventory/{productId}/check - Check Stock ============

    @Test
    @DisplayName("Should check stock availability and return true")
    void testHasEnoughStockTrue() throws Exception {
        // Arrange
        when(inventoryService.hasEnoughStock(PRODUCT_ID, 5)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get(API_PATH + "/{productId}/check", PRODUCT_ID)
                .param("quantity", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(inventoryService, times(1)).hasEnoughStock(PRODUCT_ID, 5);
        log.info("✓ Stock availability check returned true");
    }

    @Test
    @DisplayName("Should check stock availability and return false")
    void testHasEnoughStockFalse() throws Exception {
        // Arrange
        when(inventoryService.hasEnoughStock(PRODUCT_ID, 100)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get(API_PATH + "/{productId}/check", PRODUCT_ID)
                .param("quantity", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(inventoryService, times(1)).hasEnoughStock(PRODUCT_ID, 100);
        log.info("✓ Stock availability check returned false");
    }
}
