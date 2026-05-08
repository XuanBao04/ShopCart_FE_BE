package com.shopcart.order.service.impl;

import com.shopcart.constant.MessageConstant;

import com.shopcart.order.dto.request.OrderItemRequest;
import com.shopcart.order.dto.request.OrderRequest;
import com.shopcart.order.dto.response.OrderItemResponse;
import com.shopcart.order.dto.response.OrderPreviewResponse;
import com.shopcart.order.dto.response.OrderResponse;
import com.shopcart.order.entity.Order;
import com.shopcart.order.entity.OrderItem;
import com.shopcart.common.enums.OrderStatus;
import com.shopcart.common.exception.ResourceNotFoundException;
import com.shopcart.common.exception.BusinessLogicException;
import com.shopcart.order.mapper.OrderMapper;
import com.shopcart.order.repository.OrderRepository;
import com.shopcart.cart.service.ICartService;
import com.shopcart.order.service.IOrderService;
import com.shopcart.inventory.entity.Inventory;
import com.shopcart.inventory.repository.InventoryRepository;
import com.shopcart.inventory.service.IInventoryService;
import com.shopcart.product.entity.Product;
import com.shopcart.product.repository.ProductRepository;
import com.shopcart.coupon.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Order operations
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final IInventoryService inventoryService;
    private final ICartService cartService;
    private final ICouponService couponService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    private static final long SHIPPING_FEE = 29_900L;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request,String userId ) {
        // Validate và kiểm tra tồn kho
        validateOrderItems(request);

        // Tính giá
        long subtotal = calculateSubtotal(request.getOrderItems());

        // Validate và apply coupon nếu có
        long discountAmount = 0L;
        String couponCode = null;
        
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            discountAmount = couponService.calculateDiscount(request.getCouponCode(), subtotal);
            couponCode = request.getCouponCode();
        }   
       
        // Cap discount to prevent negative total price
        long actualDiscount = Math.min(subtotal, discountAmount);
        long totalPrice = subtotal - actualDiscount + SHIPPING_FEE;

        // Tạo order
        String orderId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Order order = Order.builder()
                .id(orderId)
                .userId(userId)
                .totalPrice(totalPrice)
                .shippingFee(SHIPPING_FEE)
                .discountAmount(actualDiscount)
                .couponCode(couponCode)
                .shippingAddress(request.getShippingAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .ward(request.getWard())
                .postalCode(request.getPostalCode())
                .phoneNumber(request.getPhoneNumber())
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .lastModifiedDate(now)
                .orderItems(new ArrayList<>())
                .build();

        // Tạo order items (reserve kho ngay lập tức cho PENDING)
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            inventoryService.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());

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
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(){
        List<Order> orders = orderRepository.findAllWithItems();
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.Order.NOT_FOUND + orderId));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDescWithItems(userId);
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.Order.NOT_FOUND + orderId));

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BusinessLogicException(MessageConstant.Order.CANNOT_CANCEL + order.getStatus());
        }

        // Hủy đơn / timeout -> giảm reserved_stock
        if (order.getStatus() == OrderStatus.PENDING) {
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
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstant.Order.NOT_FOUND + orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException(MessageConstant.Order.INVALID_STATUS + status);
        }

        // Admin xác nhận đơn → chuyển từ reserved → sold
        if (newStatus == OrderStatus.CONFIRMED && order.getStatus() == OrderStatus.PENDING) {
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.confirmStock(item.getProductId(), item.getQuantity());
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

        // Validate và apply coupon nếu có
        long discountAmount = 0L;
        String couponCode = null;
    
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            discountAmount = couponService.calculateDiscount(request.getCouponCode(), subtotal);
            couponCode = request.getCouponCode();
        }
        
        // Cap discount to prevent negative total price
        long actualDiscount = Math.min(subtotal, discountAmount);
        long totalPrice = subtotal - actualDiscount + SHIPPING_FEE;

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
                .discountAmount(actualDiscount)
                .couponCode(couponCode)
                .shippingFee(SHIPPING_FEE)
                .totalPrice(totalPrice)
                .build();
    }

    // ======================== Private Helper Methods ========================

    
    private void validateOrderItems(OrderRequest request) {
    if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
        throw new BusinessLogicException(MessageConstant.Order.EMPTY_ITEMS);
    }

    // 1. Tổng hợp số lượng yêu cầu theo ID sản phẩm
    Map<String, Integer> requiredQtyByProductId = request.getOrderItems().stream()
            .collect(Collectors.groupingBy(
                    OrderItemRequest::getProductId,
                    Collectors.summingInt(OrderItemRequest::getQuantity)
            ));

    Set<String> productIds = requiredQtyByProductId.keySet();

    // 2. Xác thực rằng tất cả các sản phẩm được yêu cầu đều tồn tại trong database
    List<Product> products = productRepository.findAllById(productIds);
    if (products.size() != productIds.size()) {
        Set<String> existingProductIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        // Tìm ID bị thiếu đầu tiên để trả về thông báo lỗi hữu ích
        String missingProductId = productIds.stream()
                .filter(id -> !existingProductIds.contains(id))
                .findFirst()
                .orElse("Unknown");

        throw new ResourceNotFoundException(MessageConstant.Product.NOT_FOUND + missingProductId);
    }

    // 3. Lấy thông tin tồn kho và ánh xạ chúng theo ID sản phẩm
    Map<String, Inventory> inventoryByProductId = inventoryRepository.findByProductIdIn(productIds).stream()
            .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));

    // 4. Xác thực xem có đủ hàng tồn kho cho từng sản phẩm được yêu cầu hay không
    requiredQtyByProductId.forEach((productId, requiredQuantity) -> {
        Inventory inventory = inventoryByProductId.get(productId);
        
        if (inventory == null) {
            throw new BusinessLogicException(MessageConstant.Product.INSUFFICIENT_STOCK + productId);
        }

        int reserved = inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
        int available = inventory.getQuantity() - reserved;
        
        if (available < requiredQuantity) {
            throw new BusinessLogicException(MessageConstant.Product.INSUFFICIENT_STOCK + productId);
        }
    });
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
