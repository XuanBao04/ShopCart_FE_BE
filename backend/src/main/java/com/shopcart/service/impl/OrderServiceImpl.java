package com.shopcart.service.impl;

import com.shopcart.dto.request.OrderItemRequest;
import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.exception.ResourceNotFoundException;
import com.shopcart.exception.BusinessLogicException;
import com.shopcart.mapper.OrderMapper;
import com.shopcart.repository.OrderRepository;
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

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // Validate request
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new BusinessLogicException("Order must contain at least one item");
        }
        
        // Check stock for all items
        for (OrderItemRequest item : request.getOrderItems()) {
            productService.getProductById(item.getProductId());
            if (!inventoryService.hasEnoughStock(item.getProductId(), item.getQuantity())) {
                throw new BusinessLogicException("Insufficient stock for product: " + item.getProductId());
            }
        }
        
        // Create order
        String orderId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        long totalPrice = request.getOrderItems().stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        Order order = Order.builder()
                .id(orderId)
                .userId(request.getUserId())
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .createdDate(now)
                .lastModifiedDate(now)
                .orderItems(new ArrayList<>())
                .build();
        
        // Create order items and reserve stock
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .price(itemRequest.getPrice())
                    .order(order)
                    .build();
            
            order.getOrderItems().add(orderItem);
            inventoryService.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());
        }
        
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .toList();
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
        
        // Release reserved stock
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseStock(item.getProductId(), item.getQuantity());
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setLastModifiedDate(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        try {
            order.setStatus(OrderStatus.valueOf(status));
            order.setLastModifiedDate(LocalDateTime.now());
            Order updatedOrder = orderRepository.save(order);
            return orderMapper.toOrderResponse(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Invalid order status: " + status);
        }
    }
}
