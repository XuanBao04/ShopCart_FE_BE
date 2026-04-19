package com.shopcart.service.service.impl;

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
        if (request.orderItems == null || request.orderItems.isEmpty()) {
            throw new BusinessLogicException("Order must contain at least one item");
        }
        
        // Check stock for all items
        for (OrderItemRequest item : request.orderItems) {
            productService.getProductById(item.productId);
            if (!inventoryService.hasEnoughStock(item.productId, item.quantity)) {
                throw new BusinessLogicException("Insufficient stock for product: " + item.productId);
            }
        }
        
        // Create order
        String orderId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        long totalPrice = request.orderItems.stream()
                .mapToLong(item -> item.price * item.quantity)
                .sum();
        
        Order order = new Order(orderId, request.userId, totalPrice, OrderStatus.PENDING);
        order.createdDate = now;
        order.lastModifiedDate = now;
        order.orderItems = new ArrayList<>();
        
        // Create order items and reserve stock
        for (OrderItemRequest itemRequest : request.orderItems) {
            OrderItem orderItem = new OrderItem(itemRequest.productId, itemRequest.quantity, itemRequest.price);
            orderItem.order = order;
            
            order.orderItems.add(orderItem);
            inventoryService.reserveStock(itemRequest.productId, itemRequest.quantity);
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
                .filter(o -> o.userId.equals(userId))
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
        
        if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELLED) {
            throw new BusinessLogicException("Cannot cancel order with status: " + order.status);
        }
        
        // Release reserved stock
        for (OrderItem item : order.orderItems) {
            inventoryService.releaseStock(item.productId, item.quantity);
        }
        
        order.status = OrderStatus.CANCELLED;
        order.lastModifiedDate = LocalDateTime.now();
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        try {
            order.status = OrderStatus.valueOf(status);
            order.lastModifiedDate = LocalDateTime.now();
            Order updatedOrder = orderRepository.save(order);
            return orderMapper.toOrderResponse(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Invalid order status: " + status);
        }
    }
}
