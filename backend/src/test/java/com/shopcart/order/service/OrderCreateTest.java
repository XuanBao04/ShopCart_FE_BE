package com.shopcart.order.service;

import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderPreviewResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.order.data.OrderTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Order Service — Create order")
class OrderCreateTest extends BaseOrderServiceTest {

    @Nested
    @DisplayName("TC1: createOrder() — luồng thành công chính")
    class CreateOrderHappyPath {

        @Test
        @DisplayName("TC2: Tạo đơn hàng thành công khi không sử dụng mã giảm giá")
        void createOrderWithoutCoupon() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(2)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .couponCode(null)
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .totalPrice(229_900L)
                    .shippingFee(29_900L)
                    .discountAmount(0L)
                    .status(OrderStatus.PENDING)
                    .build();

            OrderResponse expectedResponse = OrderResponse.builder().id(testOrderId).build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 2)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expectedResponse);

            OrderResponse response = orderService.createOrder(request, testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(testOrderId);
            verify(inventoryService).reserveStock(testProductId, 2);
            verify(cartService).clearCart(testUserId);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("TC3: Tạo đơn hàng thành công khi mã giảm giá hợp lệ và áp dụng giảm giá")
        void createOrderWithValidCoupon() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(1)
                    .price(1_000_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .couponCode("DISCOUNT50")
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .totalPrice(499_900L)
                    .discountAmount(500_000L)
                    .couponCode("DISCOUNT50")
                    .status(OrderStatus.PENDING)
                    .build();

            OrderResponse expectedResponse = OrderResponse.builder().id(testOrderId).build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
            when(couponService.calculateDiscount("DISCOUNT50", 1_000_000L)).thenReturn(500_000L);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(expectedResponse);

            OrderResponse response = orderService.createOrder(request, testUserId);

            assertThat(response).isNotNull();
            verify(inventoryService).reserveStock(testProductId, 1);
            verify(couponService).calculateDiscount("DISCOUNT50", 1_000_000L);
            verify(cartService).clearCart(testUserId);
        }

        @Test
        @DisplayName("TC4: Tạo đơn hàng thành công với nhiều dòng sản phẩm")
        void createOrderWithMultipleItems() {
            OrderItemRequest item1 = OrderItemRequest.builder()
                    .productId("product-1")
                    .quantity(2)
                    .price(50_000L)
                    .build();

            OrderItemRequest item2 = OrderItemRequest.builder()
                    .productId("product-2")
                    .quantity(3)
                    .price(75_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(item1, item2))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .status(OrderStatus.PENDING)
                    .build();

            when(productService.getProductById(anyString())).thenReturn(null);
            when(inventoryService.hasEnoughStock(anyString(), anyInt())).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(any())).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.createOrder(request, testUserId);

            verify(inventoryService).reserveStock("product-1", 2);
            verify(inventoryService).reserveStock("product-2", 3);
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getOrderItems()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("TC5: createOrder() — các trường hợp biên")
    class CreateOrderEdgeCases {

        @Test
        @DisplayName("TC6: Không lưu đơn và ném lỗi khi mã giảm giá không hợp lệ")
        void createOrderWithInvalidCoupon() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(1)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .couponCode("INVALID_CODE")
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
            when(couponService.calculateDiscount("INVALID_CODE", 100_000L))
                    .thenThrow(new ResourceNotFoundException("Coupon not found"));

            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                    orderService.createOrder(request, "user123"));

            assertEquals("Coupon not found", exception.getMessage());
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("TC7: Tiếp tục tạo đơn khi mã giảm giá là chuỗi rỗng")
        void createOrderWithEmptyCouponCode() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(1)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .couponCode("")
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .totalPrice(129_900L)
                    .status(OrderStatus.PENDING)
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.createOrder(request, testUserId);

            verify(couponService, never()).calculateDiscount(anyString(), anyLong());
        }

        @Test
        @DisplayName("TC8: Tạo đơn hàng chỉ với một dòng sản phẩm")
        void createOrderWithSingleItem() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(1)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .status(OrderStatus.PENDING)
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.createOrder(request, testUserId);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getOrderItems()).hasSize(1);
        }

        @Test
        @DisplayName("TC9: Ánh xạ thực thể đơn hàng khi userId bằng null")
        void createOrder_VerifyEntityMapping_AndNullUserId() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(2)
                    .price(50_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .status(OrderStatus.PENDING)
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 2)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.createOrder(request, null);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getUserId()).isNull();
            assertThat(capturedOrder.getOrderItems()).isNotNull().hasSize(1);
            OrderItem capturedItem = capturedOrder.getOrderItems().get(0);
            assertThat(capturedItem.getProductId()).isEqualTo(testProductId);
            assertThat(capturedItem.getQuantity()).isEqualTo(2);
            assertThat(capturedItem.getPrice()).isEqualTo(50_000L);
            assertThat(capturedItem.getOrder()).isSameAs(capturedOrder);
        }

        @Test
        @DisplayName("TC10: Tính tổng tiền không tràn số khi số lượng bằng Integer.MAX_VALUE")
        void createOrder_WithMaxIntegerQuantity_ShouldNotOverflow() {
            long price = 100L;
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(Integer.MAX_VALUE)
                    .price(price)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .status(OrderStatus.PENDING)
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, Integer.MAX_VALUE)).thenReturn(true);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.createOrder(request, testUserId);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order capturedOrder = captor.getValue();

            long expectedSubtotal = (long) Integer.MAX_VALUE * price;
            long expectedTotal = expectedSubtotal + 29_900L;

            assertThat(capturedOrder.getTotalPrice()).isEqualTo(expectedTotal);
            assertThat(capturedOrder.getOrderItems()).hasSize(1);
            assertThat(capturedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("TC11: previewOrder() — kiểm tra tồn kho và giá")
    class PreviewOrderTests {

        @Test
        @DisplayName("TC12: Xem trước đơn không mã giảm giá — tổng tiền gồm phí vận chuyển")
        void previewOrder_WithoutCoupon_ShouldReturnFullPrice() {
            OrderRequest request = OrderTestFactory.defaultOrderRequest();
            request.setCouponCode(null);

            OrderItemRequest item = request.getOrderItems().get(0);
            long subtotal = item.getPrice() * item.getQuantity();

            when(productService.getProductById(item.getProductId())).thenReturn(null);
            when(inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())).thenReturn(true);

            OrderPreviewResponse response = orderService.previewOrder(request);

            assertEquals(0L, response.getDiscountAmount());
            assertEquals(subtotal + OrderTestFactory.DEFAULT_SHIPPING_FEE, response.getTotalPrice());
        }

        @Test
        @DisplayName("TC13: Xem trước đơn với mã giảm giá hợp lệ — áp dụng giảm giá")
        void previewOrder_WithValidCoupon_ShouldApplyDiscount() {
            OrderRequest request = OrderTestFactory.defaultOrderRequest();
            request.setCouponCode("DISCOUNT20");

            OrderItemRequest item = request.getOrderItems().get(0);
            long subtotal = item.getPrice() * item.getQuantity();

            when(productService.getProductById(item.getProductId())).thenReturn(null);
            when(inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())).thenReturn(true);
            when(couponService.calculateDiscount("DISCOUNT20", subtotal)).thenReturn(20_000L);

            OrderPreviewResponse response = orderService.previewOrder(request);

            assertEquals(20_000L, response.getDiscountAmount());
            assertEquals("DISCOUNT20", response.getCouponCode());
        }

        @Test
        @DisplayName("TC14: Xem trước đơn khi mã chỉ gồm khoảng trắng — không gọi tính giảm giá")
        void previewOrder_WithEmptyCoupon_ShouldCoverMissingBranch() {
            OrderRequest request = OrderTestFactory.defaultOrderRequest();
            request.setCouponCode("   ");

            OrderItemRequest item = request.getOrderItems().get(0);

            when(productService.getProductById(item.getProductId())).thenReturn(null);
            when(inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())).thenReturn(true);

            OrderPreviewResponse response = orderService.previewOrder(request);

            assertEquals(0L, response.getDiscountAmount());
            verify(couponService, never()).calculateDiscount(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("TC15: createOrder() — các tình huống ngoại lệ")
    class CreateOrderExceptions {

        @Test
        @DisplayName("TC16: Ném lỗi khi danh sách sản phẩm đặt mua rỗng")
        void throwExceptionWhenOrderItemsEmpty() {
            OrderRequest request = OrderRequest.builder()
                    .orderItems(new ArrayList<>())
                    .build();

            assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                    .isInstanceOf(BusinessLogicException.class)
                    .hasMessageContaining("must contain at least one item");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC17: Ném lỗi khi danh sách sản phẩm đặt mua null")
        void throwExceptionWhenOrderItemsNull() {
            OrderRequest request = OrderRequest.builder()
                    .orderItems(null)
                    .build();

            assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                    .isInstanceOf(BusinessLogicException.class);
        }

        @Test
        @DisplayName("TC18: Ném lỗi khi tồn kho không đủ")
        void throwExceptionWhenInsufficientStock() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(100)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 100)).thenReturn(false);

            assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                    .isInstanceOf(BusinessLogicException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC19: Ném lỗi khi không tìm thấy sản phẩm")
        void throwExceptionWhenProductNotFound() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(1)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .build();

            when(productService.getProductById(testProductId))
                    .thenThrow(new ResourceNotFoundException("Product not found"));

            assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC20: Ném lỗi khi giữ kho (reserve) thất bại giữa giao dịch")
        void createOrder_WhenReserveStockFails_ShouldThrowException() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(5)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 5)).thenReturn(true);
            doThrow(new BusinessLogicException("Stock reservation failed"))
                    .when(inventoryService).reserveStock(testProductId, 5);

            assertThatThrownBy(() -> orderService.createOrder(request, testUserId))
                    .isInstanceOf(BusinessLogicException.class)
                    .hasMessageContaining("Stock reservation failed");

            verify(orderRepository, never()).save(any(Order.class));
            verify(cartService, never()).clearCart(testUserId);
        }

        @Test
        @DisplayName("TC21: Giới hạn giảm giá theo tổng tiền hàng, tránh tổng thanh toán âm")
        void createOrder_WhenDiscountExceedsSubtotal_PriceShouldNotBeNegative() {
            OrderItemRequest itemRequest = OrderItemRequest.builder()
                    .productId(testProductId)
                    .quantity(1)
                    .price(100_000L)
                    .build();

            OrderRequest request = OrderRequest.builder()
                    .orderItems(List.of(itemRequest))
                    .couponCode("SUPER_DISCOUNT")
                    .shippingAddress("123 Main St")
                    .city("Hanoi")
                    .district("Ba Dinh")
                    .ward("Truc Bach")
                    .postalCode("10000")
                    .phoneNumber("0912345678")
                    .build();

            Order savedOrder = Order.builder()
                    .id(testOrderId)
                    .userId(testUserId)
                    .status(OrderStatus.PENDING)
                    .build();

            when(productService.getProductById(testProductId)).thenReturn(null);
            when(inventoryService.hasEnoughStock(testProductId, 1)).thenReturn(true);
            when(couponService.calculateDiscount("SUPER_DISCOUNT", 100_000L)).thenReturn(150_000L);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
            when(orderMapper.toOrderResponse(savedOrder)).thenReturn(OrderResponse.builder().id(testOrderId).build());

            orderService.createOrder(request, testUserId);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            Order capturedOrder = captor.getValue();

            assertThat(capturedOrder.getDiscountAmount()).isEqualTo(100_000L);
            assertThat(capturedOrder.getTotalPrice()).isEqualTo(29_900L);
            assertThat(capturedOrder.getTotalPrice()).isGreaterThanOrEqualTo(29_900L);
        }
    }
}
