package com.shopcart.order.service.update;

import com.shopcart.order.entity.Order;
import com.shopcart.common.enums.OrderStatus;

import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.order.service.BaseOrderServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Cập nhật trạng thái đơn hàng (Trường hợp lỗi)")
class OrderUpdateStatusExceptionTest extends BaseOrderServiceTest {

    @Test
    @DisplayName("TC4: Ném lỗi khi định dạng trạng thái không hợp lệ")
    void throwExceptionForInvalidStatus() {
        Order order = Order.builder()
                .id(testOrderId)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(testOrderId, "INVALID_STATUS"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Invalid order status");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC5: Ném lỗi khi không tìm thấy đơn trong quá trình cập nhật trạng thái")
    void throwExceptionWhenOrderNotFoundDuringUpdate() {
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(testOrderId, "CONFIRMED"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
