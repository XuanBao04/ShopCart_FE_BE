package com.shopcart.service.impl;

import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderItemResponse;
import com.shopcart.dto.response.OrderPreviewResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.mapper.OrderMapper;
import com.shopcart.repository.OrderRepository;
import com.shopcart.service.ICartService;
import com.shopcart.service.IOrderService;
import com.shopcart.service.IInventoryService;
import com.shopcart.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Order operations
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final IInventoryService inventoryService;
    private final IProductService productService;
    private final ICartService cartService;

    private static final long SHIPPING_FEE = 29_900L;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request,String userId ) {
        // Validate và kiểm tra tồn kho
        validateOrderItems(request);

        // Tính giá
        long subtotal = calculateSubtotal(request.getOrderItems());
        long totalPrice = subtotal + SHIPPING_FEE;

        // Tạo order
        String orderId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .totalPrice(totalPrice)
                .shippingFee(SHIPPING_FEE)
                .status(OrderStatus.PENDING)
                .createdDate(now)
                .lastModifiedDate(now)
                .orderItems(new ArrayList<>())
                .build();

        // Tạo order items (chưa trừ kho, chờ admin xác nhận mới trừ)
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .order(order)
                    .build();

            order.getOrderItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        cartService.clearCart(userId);

        return orderMapper.toOrderResponse(savedOrder);
    }
    @Override
    public List<OrderResponse> getAllOrders(){
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessLogicException("Cannot cancel order with status: " + order.getStatus());
        }

        // Chỉ hoàn kho nếu đơn đã được xác nhận (đã trừ kho trước đó)
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.releaseStock(item.getProductId(), item.getQuantity());
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setLastModifiedDate(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Invalid order status: " + status);
        }

        // Admin xác nhận đơn → trừ tồn kho
        if (newStatus == OrderStatus.CONFIRMED && order.getStatus() == OrderStatus.PENDING) {
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.reserveStock(item.getProductId(), item.getQuantity());
            }
        }

        order.setStatus(newStatus);
        order.setLastModifiedDate(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderPreviewResponse previewOrder(OrderRequest request) {
        // Validate và kiểm tra tồn kho
        validateOrderItems(request);

        // Tính giá
        long subtotal = calculateSubtotal(request.getOrderItems());
        long totalPrice = subtotal + SHIPPING_FEE;

        // Build danh sách items cho preview
        List<OrderItemResponse> previewItems = request.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        return OrderPreviewResponse.builder()
                .userId(request.getUserId())
                .items(previewItems)
                .subtotal(subtotal)
                .shippingFee(SHIPPING_FEE)
                .totalPrice(totalPrice)
                .build();
    }

    // ======================== Private Helper Methods ========================

    /*
     * Validate order items: không rỗng, sản phẩm tồn tại, đủ tồn kho.
     */
    private void validateOrderItems(OrderRequest request) {
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new BusinessLogicException("Order must contain at least one item");
        }

        for (OrderItemRequest item : request.getOrderItems()) {
            productService.getProductById(item.getProductId());
            if (!inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())) {
                throw new BusinessLogicException("Insufficient stock for product: " + item.getProductId());
            }
        }
    }

    /*
     * Tính tổng tiền hàng (chưa bao gồm phí ship).
     */
    private long calculateSubtotal(List<OrderItemRequest> items) {
        return items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
