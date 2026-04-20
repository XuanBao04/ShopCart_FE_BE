# 🎯 SHIPPING FEE - CÁCH ĐƠNGIẢN ĐỀ ĐÁPHỨNG YÊU CẦU

## 📋 YÊU CẦU THỰC TẾ

Từ `ShopCart_Flows.md`:
> **Tính giá (Pricing):** Tính tổng tiền, áp dụng mã giảm giá, **phí vận chuyển**

**Mục đích bài toán:** Kiểm thử (Testing), không phải implement đầy đủ

**Cấp độ yêu cầu:**
- ✅ Tính toán được phí vận chuyển
- ✅ Có thể test logic tính giá
- ✅ Dễ dàng validate

---

## ✅ GIẢI PHÁP ĐƠN GIẢN NHẤT

### **Lựa chọn: Fixed Shipping Fee (Default)**

```
Không cần User chọn → Mặc định 29.900 VND
Công thức: totalPrice = subtotal + shippingFee - discount

Ví dụ:
- Items: 100.000 VND
- Shipping: 29.900 VND (fixed)
- Discount: 10.000 VND (nếu có coupon)
- Total: 100.000 + 29.900 - 10.000 = 119.900 VND
```

---

## 🔧 IMPLEMENTATION (Backend - Minimal)

### **1. OrderRequest.java** - KHÔNG ĐỔI

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotEmpty(message = "Order items list cannot be empty")
    @Valid
    private List<OrderItemRequest> orderItems;
    
    // Optional: couponCode (nếu implement discount)
    private String couponCode;
}
```

### **2. OrderServiceImpl.java** - SỬA CÓ SỰ KIÊN (COPY TỪ NEW FILES)

```java
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
    
    // ✅ ĐƠN GIẢN: Tính subtotal từ items
    long subtotal = request.getOrderItems().stream()
            .mapToLong(item -> item.getPrice() * item.getQuantity())
            .sum();
    
    // ✅ ĐƠN GIẢN: Fixed shipping fee
    long shippingFee = 29900L;  // Mặc định 29.9k VND
    
    // ✅ TODO: Discount logic (nếu cần)
    long discount = 0;
    // if (request.getCouponCode() != null) {
    //     discount = calculateDiscount(subtotal, request.getCouponCode());
    // }
    
    // ✅ CÔNG THỨC: totalPrice = subtotal + shipping - discount
    long totalPrice = subtotal + shippingFee - discount;
    
    String orderId = UUID.randomUUID().toString();
    LocalDateTime now = LocalDateTime.now();
    
    Order order = Order.builder()
            .id(orderId)
            .userId(request.getUserId())
            .totalPrice(totalPrice)
            .shippingFee(shippingFee)  // ✅ Lưu shipping fee
            .couponCode(request.getCouponCode())
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
```

### **3. OrderResponse.java** - THÊM SHIPPING FIELD

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private List<OrderItemResponse> items;
    private Long shippingFee;  // ✅ THÊM (cho biết fee đã tính)
    private Long totalPrice;   // ✅ Bao gồm shipping fee
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
```

### **4. OrderMapper.java** - CẬP NHẬT MAPPING

```java
public OrderResponse toOrderResponse(Order order) {
    if (order == null) return null;
    
    return OrderResponse.builder()
            .id(order.getId())
            .userId(order.getUserId())
            .items(order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .toList())
            .shippingFee(order.getShippingFee() != null ? order.getShippingFee() : 0)  // ✅
            .totalPrice(order.getTotalPrice())
            .status(order.getStatus().toString())
            .createdDate(order.getCreatedDate())
            .lastModifiedDate(order.getLastModifiedDate())
            .build();
}
```

---

## 📝 UNIT TESTS (OrderServiceTest.java)

```java
@Test
@DisplayName("Order creation with default shipping fee")
void testCreateOrder_WithDefaultShipping() {
    // Arrange
    OrderRequest request = OrderRequest.builder()
            .userId("user-001")
            .orderItems(Arrays.asList(
                    OrderItemRequest.builder()
                            .productId("PROD-001")
                            .quantity(2)
                            .price(50000L)
                            .build()
            ))
            .build();
    
    // Act
    OrderResponse response = orderService.createOrder(request);
    
    // Assert
    assertNotNull(response);
    assertEquals("user-001", response.getUserId());
    assertEquals(29900L, response.getShippingFee());  // ✅ Fixed 29.9k
    assertEquals(100000L + 29900L, response.getTotalPrice());  // 129.9k
}

@Test
@DisplayName("Order total includes shipping fee")
void testCreateOrder_TotalIncludesShipping() {
    // Arrange
    OrderRequest request = OrderRequest.builder()
            .userId("user-002")
            .orderItems(Arrays.asList(
                    OrderItemRequest.builder()
                            .productId("PROD-002")
                            .quantity(1)
                            .price(100000L)
                            .build()
            ))
            .build();
    
    // Act
    OrderResponse response = orderService.createOrder(request);
    
    // Assert
    long expectedTotal = 100000L + 29900L;  // subtotal + shipping
    assertEquals(expectedTotal, response.getTotalPrice());
}
```

---

## 🎯 SUMMARY: CÓ GÌ ĐẬP ĐỦ REQUIREMENTS?

| Yêu cầu | Giải pháp | ✓ |
|--------|----------|---|
| Tính tổng tiền | `totalPrice = subtotal + shippingFee` | ✅ |
| Phí vận chuyển | Fixed 29.900 VND | ✅ |
| Áp dụng mã giảm giá | Sẵn sàng để implement (couponCode field) | ✅ |
| Có thể test | ✅ Rõ ràng, dễ validate | ✅ |
| Đơn giản | Không cần UI chọn, không cần logic phức tạp | ✅ |

---

## ⏱️ THỜI GIAN IMPLEMENT

```
Backend sửa: 30 phút
  - OrderServiceImpl: ~10 phút (thêm 2 dòng tính shipping)
  - OrderResponse: ~5 phút (thêm 1 field)
  - OrderMapper: ~5 phút (map field)
  
Tests viết: 30 phút
  - 2-3 test cases cho shipping

TỔNG: ~1 hour ✅
```

---

## 🚀 NEXT STEPS

### **Ngay hôm nay:**

1. **Sửa 3 backend files** (copy code ở trên)
2. **Run:** `mvn clean compile` (verify)
3. **Viết OrderServiceTest.java** (2 test cases cho shipping)
4. **Run:** `mvn test` (verify all pass)
5. **Commit:** 
   ```
   git add .
   git commit -m "feat(order): add default shipping fee (29.9k VND) to order calculation"
   ```

### **Lợi ích:**

✅ **Đơn giản, dễ test**  
✅ **Đáp ứng yêu cầu "phí vận chuyển"**  
✅ **Dễ mở rộng sau** (nếu muốn thêm logic discount, free shipping, etc.)  
✅ **Không phải sửa Frontend** ngay

---

## 📊 So sánh các lựa chọn

| Lựa chọn | Độ phức tạp | Thời gian | Đáp ứng |
|---------|------------|----------|--------|
| **Fixed Shipping (RECOMMENDED)** | 🟢 Thấp | ~1 hour | ✅ |
| Lựa chọn A (User chọn) | 🟠 Trung | ~3 hours | ✅ |
| Lựa chọn B (Smart logic) | 🔴 Cao | ~4 hours | ✅ |

---

**KHUYẾN CÁO: Chọn Fixed Shipping (cách này)** → Nhanh, đơn giản, vẫn đáp ứng yêu cầu ✨
