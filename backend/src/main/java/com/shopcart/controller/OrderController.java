package com.shopcart.controller;

import com.shopcart.dto.request.OrderRequest;
import com.shopcart.dto.response.OrderPreviewResponse;
import com.shopcart.dto.response.OrderResponse;
import com.shopcart.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Order operations
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping("/{userId}")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request, @PathVariable String userId) {
        OrderResponse response = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> previewOrder(@RequestBody OrderRequest request) {
        OrderPreviewResponse response = orderService.previewOrder(request);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders(){
        List<OrderResponse> responses = orderService.getAllOrders();
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(authentication, #userId)")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable String userId) {
        List<OrderResponse> responses = orderService.getUserOrders(userId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{orderId}/{status}")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @PathVariable String status) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }
}
