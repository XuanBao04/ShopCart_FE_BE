# ShopCart - Business Flows Documentation

Chi tiết mô tả các luồng xử lý chính của hệ thống ShopCart E-commerce.

---

## 📋 Mục Lục

1. [Luồng Giỏ Hàng (Cart Flow)](#luồng-giỏ-hàng-cart-flow)
2. [Luồng Thanh Toán (Checkout Flow)](#luồng-thanh-toán-checkout-flow)
3. [Luồng Xử lý Đơn Hàng (Order Processing Flow)](#luồng-xử-lý-đơn-hàng-order-processing-flow)

---

## 🛒 Luồng Giỏ Hàng (Cart Flow)

Giai đoạn người dùng chọn sản phẩm và thêm vào giỏ. Điểm mấu chốt là kiểm tra tồn kho liên tục.

### 📊 Sơ đồ Sequence

```
Frontend                 Controller              CartService           Repository
   |                          |                       |                    |
   |---POST /api/cart/add---->|                       |                    |
   |   CartItemRequest        |                       |                    |
   |                          |---validate input----->|                    |
   |                          |                       |                    |
   |                          |---check product-------|--query ProductRepo-|
   |                          |                       |                    |
   |                          |---check stock---------|--query Inventory---|
   |                          |                       |                    |
   |                          |---addToCart/update----|--save CartItem-----|
   |                          |                       |                    |
   |<--CartResponse-----------|<---return response----|                    |
   |   (success, totalPrice)  |                       |                    |
   |                          |                       |                    |
```

### 🔄 Chi tiết từng bước

#### **Bước 1: Khởi tạo Yêu Cầu**

**Frontend (React):**
```javascript
// Người dùng nhấn "Thêm vào giỏ hàng"
const addToCart = async (productId, quantity) => {
  const response = await axios.post('/api/cart/add', {
    productId: 'laptop-dell-001',
    quantity: 2
  });
  // Cập nhật Cart Badge
  updateCartBadge(response.data.totalItems);
};
```

**Request:**
```json
POST /api/cart/{userId}/add
Content-Type: application/json

{
  "productId": "laptop-dell-001",
  "quantity": 2
}
```

---

#### **Bước 2: Xác Thực (Validation)**

**CartController:**
```java
@PostMapping("/{userId}/add")
public ResponseEntity<CartResponse> addToCart(
    @PathVariable String userId,
    @RequestBody @Valid CartItemRequest request) {
    
    // Spring Boot tự động validate:
    // - quantity > 0
    // - productId không rỗng
    // - Nếu lỗi → 400 Bad Request
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(cartService.addToCart(userId, request));
}
```

**Xác thực:**
- ✅ `productId` không null/rỗng
- ✅ `quantity` > 0
- ✅ `userId` hợp lệ

**Nếu lỗi:**
```json
{
  "status": 400,
  "error": "Invalid Input",
  "message": "Quantity must be greater than 0"
}
```

---

#### **Bước 3: Xử lý Nghiệp Vụ (CartService)**

**Kiểm tra sản phẩm:**
```java
// Gọi ProductRepository
Product product = productRepository.findById(productId)
    .orElseThrow(() -> new ResourceNotFoundException(
        "Product not found with id: " + productId
    ));

// Kiểm tra trạng thái
if (product.getStatus() != ProductStatus.ACTIVE) {
    throw new BusinessLogicException(
        "Product is not available for purchase"
    );
}
```

**Kiểm tra tồn kho:**
```java
// Gọi InventoryRepository
Inventory inventory = inventoryRepository
    .findByProductId(productId)
    .orElseThrow(() -> new ResourceNotFoundException(
        "Inventory not found"
    ));

if (inventory.getQuantity() < quantity) {
    throw new BusinessLogicException(
        "Insufficient stock. Available: " + 
        inventory.getQuantity() + 
        ", Requested: " + quantity
    );
}
```

**Xử lý Cart Item:**
```java
// Kiểm tra xem item đã tồn tại trong giỏ chưa
Optional<CartItem> existingItem = 
    cartRepository.findByUserIdAndProductId(userId, productId);

CartItem cartItem;
if (existingItem.isPresent()) {
    // Cộng dồn số lượng
    cartItem = existingItem.get();
    cartItem.setQuantity(cartItem.getQuantity() + quantity);
} else {
    // Tạo mới
    cartItem = CartItem.builder()
        .userId(userId)
        .productId(productId)
        .quantity(quantity)
        .build();
}

cartRepository.save(cartItem);
```

---

#### **Bước 4: Lưu & Phản Hồi**

**CartService trả về CartResponse:**
```java
List<CartItem> cartItems = cartRepository.findByUserId(userId);
CartResponse response = cartMapper.toCartResponse(userId, cartItems);
```

**Response:**
```json
{
  "userId": "user123",
  "items": [
    {
      "id": 1,
      "productId": "laptop-dell-001",
      "quantity": 2
    }
  ],
  "totalItems": 1,
  "totalPrice": 59980
}
```

**Frontend cập nhật UI:**
```javascript
// Hiển thị thông báo thành công
showToast("Thêm sản phẩm vào giỏ thành công!");

// Cập nhật Cart Badge
updateCartBadge(response.totalItems);

// Cập nhật giỏ hàng trên giao diện
setCartItems(response.items);
```

---

## 💰 Luồng Thanh Toán (Checkout Flow)

Khi người dùng vào trang Giỏ hàng và quyết định mua hàng.

### 📊 Sơ đồ Sequence

```
Frontend              CheckoutComponent          Backend Services
   |                        |                           |
   |--Display Cart Items----|                           |
   |  + Calculate Subtotal  |                           |
   |  + Apply Discount Code |                           |
   |  + Calculate Shipping  |                           |
   |                        |                           |
   |<--Display Pricing------|                           |
   |                        |                           |
   |--User enters address---|                           |
   |  and confirms order    |                           |
   |                        |                           |
   |--POST /api/orders------|--Validate stock again-----|
   |  OrderRequest          |                           |
   |                        |--Check each item---------|
   |                        |                           |
   |<--OrderResponse--------|<--201 Created            |
   |  (OrderId, Total)      |                           |
   |                        |                           |
   |--Redirect to-----------|                           |
   | Order Confirmation     |                           |
   |                        |                           |
```

### 🔄 Chi tiết từng bước

#### **Bước 1: Tính Toán Giá (Pricing)**

**Frontend Component:**
```javascript
const CheckoutPage = () => {
  const [cart, setCart] = useState(null);
  const [couponCode, setCouponCode] = useState('');
  
  useEffect(() => {
    // Lấy giỏ hàng
    const cartResponse = await cartService.getCart(userId);
    
    // Tính toán
    const subtotal = cartResponse.items.reduce(
      (sum, item) => sum + (item.price * item.quantity),
      0
    );
    
    // Áp dụng coupon
    const discount = applyCoupon(couponCode, subtotal);
    
    // Tính shipping
    const shipping = subtotal > 50 ? 0 : 9.99;
    
    // Tổng tiền
    const total = subtotal - discount + shipping;
    
    setCart({
      subtotal,
      discount,
      shipping,
      total
    });
  }, [couponCode]);
};
```

**Hiển thị:**
```
Subtotal:        59,980 VND
Discount (10%):  -5,998 VND
Shipping Fee:     9,990 VND
─────────────────────────
Total:           63,972 VND
```

---

#### **Bước 2: Gửi Yêu Cầu Đặt Hàng**

**Frontend:**
```javascript
const submitOrder = async () => {
  const orderRequest = {
    userId: 'user123',
    orderItems: [
      {
        productId: 'laptop-dell-001',
        quantity: 2,
        price: 29990
      }
    ],
    couponCode: 'SAVE10',
    shippingFee: 9990
  };
  
  const response = await orderService.createOrder(orderRequest);
  // Chuyển hướng đến trang xác nhận
  navigate(`/order/${response.id}`);
};
```

**Request:**
```json
POST /api/orders
Content-Type: application/json

{
  "userId": "user123",
  "orderItems": [
    {
      "productId": "laptop-dell-001",
      "quantity": 2,
      "price": 29990
    }
  ],
  "couponCode": "SAVE10",
  "shippingFee": 9990
}
```

---

## 📦 Luồng Xử lý Đơn Hàng (Order Processing Flow)

Luồng phức tạp nhất, đòi hỏi tính toàn vẹn dữ liệu cao (Transaction).

### 📊 Sơ đồ Sequence

```
Frontend          Controller      OrderService       Repositories      Database
   |                 |                  |                 |               |
   |--POST /orders-->|                  |                 |               |
   |                 |--validate input->|                 |               |
   |                 |                  |                 |               |
   |                 |--check stock-----|--query Inventory|--check each---|
   |                 |   again          |                 |  product qty  |
   |                 |                  |                 |               |
   |                 |<--all OK---------|                 |               |
   |                 |                  |                 |               |
   |                 |--create Order----|--save Order-----|--INSERT----->|
   |                 |                  |                 |               |
   |                 |--create OrderItems|save Items------|--INSERT----->|
   |                 |                  |                 |               |
   |                 |--decrease Stock--|--update Inventory|--UPDATE----->|
   |                 |                  |                 |               |
   |                 |--clear CartItems-|--delete cart----|--DELETE----->|
   |                 |                  |                 |               |
   |<--201 Created---|<--OrderResponse--|                 |               |
   |  OrderId        |                  |                 |               |
   |                 |                  |                 |               |
```

### 🔄 Chi tiết từng bước

#### **Bước 1: Chốt Tồn Kho Cuối Cùng**

**OrderService:**
```java
@Transactional
public OrderResponse createOrder(OrderRequest request) {
    // Validate request
    validateOrderRequest(request);
    
    // 🔴 CRITICAL: Check stock one more time
    // (Vì có thể người khác mua trong lúc user checkout)
    for (OrderItemRequest item : request.getOrderItems()) {
        if (!inventoryService.hasEnoughStock(
            item.getProductId(), 
            item.getQuantity())) {
            
            throw new BusinessLogicException(
                "Product " + item.getProductId() + 
                " is no longer available in requested quantity"
            );
        }
    }
    
    // Tiếp tục tạo đơn hàng...
}
```

**Xác thực:**
- ✅ Toàn bộ sản phẩm vẫn còn đủ tồn kho
- ✅ Sản phẩm vẫn ACTIVE
- ✅ User có quyền đặt hàng
- ✅ Thông tin giao hàng đầy đủ

---

#### **Bước 2: Tạo Đơn Hàng**

**Tính toán tổng tiền:**
```java
// Tính subtotal
Long subtotal = request.getOrderItems().stream()
    .mapToLong(item -> item.getPrice() * item.getQuantity())
    .sum();

// Áp dụng discount
Long discount = applyCouponDiscount(request.getCouponCode(), subtotal);

// Tính phí vận chuyển
Long shippingFee = request.getShippingFee() != null ? 
    request.getShippingFee() : 0;

// Tổng tiền
Long totalPrice = subtotal - discount + shippingFee;
```

**Tạo Order:**
```java
Order order = Order.builder()
    .userId(request.getUserId())
    .status("PENDING")      // Trạng thái ban đầu
    .totalPrice(totalPrice)
    .discount(discount)
    .shippingFee(shippingFee)
    .couponCode(request.getCouponCode())
    .createdDate(LocalDateTime.now())
    .build();

// Tạo OrderItems
List<OrderItem> orderItems = request.getOrderItems()
    .stream()
    .map(item -> OrderItem.builder()
        .order(order)
        .productId(item.getProductId())
        .quantity(item.getQuantity())
        .price(item.getPrice())
        .build())
    .collect(Collectors.toList());

order.setOrderItems(orderItems);

// Lưu xuống database
Order savedOrder = orderRepository.save(order);
```

---

#### **Bước 3: Trừ Tồn Kho**

**InventoryService:**
```java
@Transactional
public void decreaseStock(String productId, Integer quantity) {
    Inventory inventory = inventoryRepository
        .findByProductId(productId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Inventory not found for product: " + productId
        ));
    
    Integer newStock = inventory.getQuantity() - quantity;
    
    if (newStock < 0) {
        throw new BusinessLogicException(
            "Cannot decrease stock below 0"
        );
    }
    
    inventory.setQuantity(newStock);
    inventoryRepository.save(inventory);
    
    log.info("Stock decreased for product {} from {} to {}", 
        productId, inventory.getQuantity(), newStock);
}
```

**Giảm tồn kho cho mỗi sản phẩm:**
```java
for (OrderItem item : order.getOrderItems()) {
    inventoryService.decreaseStock(
        item.getProductId(), 
        item.getQuantity()
    );
}
```

---

#### **Bước 4: Dọn Dẹp & Phản Hồi**

**Xóa CartItems:**
```java
// Xóa tất cả item trong giỏ sau khi đặt hàng
cartRepository.deleteByUserId(request.getUserId());

log.info("Cart cleared for user: {}", request.getUserId());
```

**Trả về OrderResponse:**
```java
OrderResponse response = orderMapper.toOrderResponse(savedOrder);
```

**Response:**
```json
{
  "id": "ORD-2024-001",
  "userId": "user123",
  "items": [
    {
      "id": 1,
      "productId": "laptop-dell-001",
      "quantity": 2,
      "price": 29990
    }
  ],
  "status": "PENDING",
  "totalPrice": 63972,
  "discount": 5998,
  "shippingFee": 9990,
  "createdDate": "2024-04-18T10:30:00"
}
```

---

#### **Bước 5: Cập Nhật Giao Diện**

**Frontend:**
```javascript
const submitOrder = async (orderRequest) => {
  try {
    const response = await orderService.createOrder(orderRequest);
    
    // Hiển thị thông báo thành công
    showToast("Đặt hàng thành công!", "success");
    
    // Xóa cache giỏ hàng
    clearCartCache();
    
    // Chuyển hướng
    setTimeout(() => {
      navigate(`/order-confirmation/${response.id}`);
    }, 1500);
    
  } catch (error) {
    // Hiển thị lỗi
    if (error.response?.status === 422) {
      showToast(error.response.data.message, "error");
    } else {
      showToast("Lỗi khi đặt hàng. Vui lòng thử lại!", "error");
    }
  }
};
```

**Order Confirmation Page:**
```javascript
const OrderConfirmation = ({ orderId }) => {
  const [order, setOrder] = useState(null);
  
  useEffect(() => {
    orderService.getOrderById(orderId)
      .then(order => {
        setOrder(order);
        
        // Gửi email confirmation
        emailService.sendOrderConfirmation(order);
        
        // Trigger SMS (optional)
        smsService.sendOrderNotification(order);
      });
  }, [orderId]);
  
  return (
    <div className="confirmation-container">
      <h1>✅ Đặt hàng thành công!</h1>
      <p>Mã đơn hàng: {order?.id}</p>
      <p>Tổng tiền: {formatPrice(order?.totalPrice)}</p>
      <p>Trạng thái: {order?.status}</p>
      <button onClick={() => navigate('/orders')}>
        Xem đơn hàng của bạn
      </button>
    </div>
  );
};
```

---

## 🔐 Xử lý Lỗi & Exception Cases

### Cart Flow

| Lỗi | Status | Response |
|-----|--------|----------|
| Invalid Input | 400 | `InvalidInputException` |
| Product Not Found | 404 | `ResourceNotFoundException` |
| Product Not Active | 422 | `BusinessLogicException` |
| Insufficient Stock | 422 | `BusinessLogicException` |
| User Not Found | 404 | `ResourceNotFoundException` |

### Order Flow

| Lỗi | Status | Response |
|-----|--------|----------|
| Invalid Order Request | 400 | `InvalidInputException` |
| Stock Changed During Checkout | 422 | `BusinessLogicException` |
| Payment Failed | 402 | `PaymentException` |
| Order Not Found | 404 | `ResourceNotFoundException` |

---

## 🔄 Transaction Boundaries

### 🔴 CRITICAL Operations (Needs @Transactional)

1. **addToCart** - Modify CartItem
2. **createOrder** - Create Order, OrderItems, Update Inventory, Clear Cart
3. **decreaseStock** - Update Inventory
4. **cancelOrder** - Update Order status, Release Inventory

### ✅ Read-only Operations (readOnly=true)

1. **getCart** - Read CartItems
2. **getOrderById** - Read Order
3. **getUserOrders** - Read Orders
4. **getStock** - Read Inventory

---

## 📊 Database Transaction Isolation

```
Isolation Level: READ_COMMITTED (Default)

Order Processing Transaction:
╔════════════════════════════════════════╗
║ BEGIN TRANSACTION                      ║
├────────────────────────────────────────┤
║ 1. Insert Order                        ║
║ 2. Insert OrderItems                   ║
║ 3. Update Inventory (Decrease)         ║
║ 4. Delete CartItems                    ║
├────────────────────────────────────────┤
║ COMMIT (nếu thành công)                ║
║ ROLLBACK (nếu lỗi ở bất kỳ bước nào)  ║
╚════════════════════════════════════════╝

Nếu lỗi ở bước 3: toàn bộ transaction rollback
→ Order không được tạo
→ Inventory không thay đổi
→ CartItems vẫn còn
```

---

## 🧪 Test Cases cần viết

### CartService Tests
- [ ] `addToCart_withValidInput_shouldAddItem`
- [ ] `addToCart_withInsufficientStock_shouldThrowException`
- [ ] `addToCart_whenProductNotActive_shouldThrowException`
- [ ] `addToCart_whenItemExists_shouldUpdateQuantity`
- [ ] `removeFromCart_shouldRemoveItem`
- [ ] `updateQuantity_withZero_shouldDeleteItem`

### OrderService Tests
- [ ] `createOrder_withValidInput_shouldCreateOrder`
- [ ] `createOrder_shouldCheckStockAgain`
- [ ] `createOrder_shouldDecreaseInventory`
- [ ] `createOrder_shouldClearCart`
- [ ] `createOrder_withStockChangeAtCheckout_shouldThrow`
- [ ] `cancelOrder_shouldReleaseStock`

### InventoryService Tests
- [ ] `decreaseStock_shouldUpdateQuantity`
- [ ] `decreaseStock_withInsufficientStock_shouldThrow`
- [ ] `releaseStock_shouldIncreaseQuantity`
- [ ] `hasEnoughStock_shouldReturnCorrectValue`

---

## 📚 Tài Liệu Tham Khảo

- [Spring @Transactional](https://spring.io/guides/gs/managing-transactions/)
- [Database Transactions](https://en.wikipedia.org/wiki/Database_transaction)
- [E-commerce Best Practices](https://www.shopify.com/blog/)
