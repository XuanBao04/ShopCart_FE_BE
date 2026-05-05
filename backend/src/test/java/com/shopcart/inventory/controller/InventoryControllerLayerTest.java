package com.shopcart.inventory.controller;

import com.shopcart.inventory.controller.InventoryController;
import com.shopcart.inventory.dto.request.UpdateStockRequest;
import com.shopcart.inventory.service.IInventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Inventory Controller — Kiểm thử lớp Controller")
class InventoryControllerLayerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IInventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Nên lấy được số lượng tồn kho")
    void getStock_Success() throws Exception {
        when(inventoryService.getStock("P1")).thenReturn(100);

        mockMvc.perform(get("/api/inventory/P1"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));
    }

    @Test
    @DisplayName("Nên cập nhật được số lượng tồn kho")
    void updateStock_Success() throws Exception {
        UpdateStockRequest request = UpdateStockRequest.builder().quantity(50).build();

        mockMvc.perform(patch("/api/inventory/P1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(inventoryService).updateStock("P1", 50);
    }

    @Test
    @DisplayName("Nên giữ được kho hàng (Reserve)")
    void reserveStock_Success() throws Exception {
        mockMvc.perform(post("/api/inventory/P1/reserve")
                .param("quantity", "10"))
                .andExpect(status().isNoContent());

        verify(inventoryService).reserveStock("P1", 10);
    }

    @Test
    @DisplayName("Nên giải phóng được kho hàng (Release)")
    void releaseStock_Success() throws Exception {
        mockMvc.perform(post("/api/inventory/P1/release")
                .param("quantity", "5"))
                .andExpect(status().isNoContent());

        verify(inventoryService).releaseStock("P1", 5);
    }

    @Test
    @DisplayName("Nên kiểm tra được tính khả dụng của kho hàng")
    void hasEnoughStock_Success() throws Exception {
        when(inventoryService.hasEnoughStock("P1", 20)).thenReturn(true);

        mockMvc.perform(get("/api/inventory/P1/check")
                .param("quantity", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
