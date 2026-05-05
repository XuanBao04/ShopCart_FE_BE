package com.shopcart.order.service.get;

import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Truy vấn đơn hàng (Trường hợp lỗi)")
class OrderGetExceptionTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("Nên ném lỗi ResourceNotFoundException khi không tìm thấy đơn hàng theo ID")
    void getOrderByIdNotFound() {
        when(orderRepository.findById("INVALID_ID")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> orderService.getOrderById("INVALID_ID")
        );

        assertEquals("Order not found with id: INVALID_ID", exception.getMessage());
    }
}
