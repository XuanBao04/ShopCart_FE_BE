package com.shopcart.config;

import com.shopcart.entity.CartItem;
import com.shopcart.entity.Coupon;
import com.shopcart.entity.Inventory;
import com.shopcart.entity.Order;
import com.shopcart.entity.OrderItem;
import com.shopcart.entity.Product;
import com.shopcart.entity.User;
import com.shopcart.entity.enums.OrderStatus;
import com.shopcart.entity.enums.ProductStatus;
import com.shopcart.entity.enums.UserRole;
import com.shopcart.repository.CartRepository;
import com.shopcart.repository.CouponRepository;
import com.shopcart.repository.InventoryRepository;
import com.shopcart.repository.OrderRepository;
import com.shopcart.repository.ProductRepository;
import com.shopcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Initializer - Creates default users on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User customer = createUserIfMissing("customer1", "password123", "Customer One", "customer1@shopcart.com", UserRole.CUSTOMER);
        createUserIfMissing("admin", "admin123", "Administrator", "admin@shopcart.com", UserRole.ADMIN);

        createDefaultProducts();
        createDefaultCoupons();
        createDefaultCartItems(customer);
        createDefaultOrders(customer);
    }

    private User createUserIfMissing(String username, String rawPassword, String fullName, String email, UserRole role) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = User.builder()
                            .username(username)
                            .password(passwordEncoder.encode(rawPassword))
                            .fullName(fullName)
                            .email(email)
                            .role(role)
                            .build();
                    User saved = userRepository.save(user);
                    log.info("Created default user: {}", username);
                    return saved;
                });
    }

    private void createDefaultProducts() {
        List<Product> products = List.of(
                Product.builder().id("P001").name("iPhone 15 128GB").price(21990000L)
                        .description("Smartphone Apple A16, man hinh Super Retina XDR 6.1 inch").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P002").name("Samsung Galaxy S24").price(18990000L)
                        .description("Smartphone Android cao cap, man hinh Dynamic AMOLED 2X").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P003").name("Xiaomi 14").price(15990000L)
                        .description("Smartphone Snapdragon 8 Gen 3, camera Leica").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P004").name("MacBook Air M3 13 inch").price(28990000L)
                        .description("Laptop mong nhe chip Apple M3, pin toi uu").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P005").name("Dell XPS 13").price(32990000L)
                        .description("Ultrabook cao cap, man hinh InfinityEdge").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P006").name("iPad Air 11 inch").price(16990000L)
                        .description("May tinh bang phuc vu hoc tap va cong viec").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P007").name("Sony WH-1000XM5").price(7990000L)
                        .description("Tai nghe chong on chu dong, chat am chi tiet").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P008").name("Logitech MX Master 3S").price(2490000L)
                        .description("Chuot khong day cho dan van phong va designer").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P009").name("Keychron K8 Pro").price(2790000L)
                        .description("Ban phim co wireless ho tro macOS va Windows").status(ProductStatus.ACTIVE).build(),
                Product.builder().id("P010").name("Anker 737 Power Bank").price(3290000L)
                        .description("Pin du phong dung luong lon, sac nhanh 140W").status(ProductStatus.ACTIVE).build()
        );

        List<Product> missingProducts = products.stream()
                .filter(p -> !productRepository.existsById(p.getId()))
                .toList();
        if (!missingProducts.isEmpty()) {
            productRepository.saveAll(missingProducts);
            log.info("Seeded {} products", missingProducts.size());
        }

        List<Inventory> inventories = List.of(
                Inventory.builder().productId("P001").quantity(25).build(),
                Inventory.builder().productId("P002").quantity(30).build(),
                Inventory.builder().productId("P003").quantity(28).build(),
                Inventory.builder().productId("P004").quantity(12).build(),
                Inventory.builder().productId("P005").quantity(10).build(),
                Inventory.builder().productId("P006").quantity(20).build(),
                Inventory.builder().productId("P007").quantity(35).build(),
                Inventory.builder().productId("P008").quantity(50).build(),
                Inventory.builder().productId("P009").quantity(40).build(),
                Inventory.builder().productId("P010").quantity(32).build()
        );

        List<Inventory> missingInventories = inventories.stream()
                .filter(i -> inventoryRepository.findByProductId(i.getProductId()).isEmpty())
                .toList();
        if (!missingInventories.isEmpty()) {
            inventoryRepository.saveAll(missingInventories);
            log.info("Seeded {} inventory records", missingInventories.size());
        }
    }

    private void createDefaultCoupons() {
        if (couponRepository.count() > 0) {
            return;
        }
        List<Coupon> coupons = List.of(
                Coupon.builder().code("WELCOME10").discountPercent(10).active(true).build(),
                Coupon.builder().code("SALE15").discountPercent(15).active(true).build(),
                Coupon.builder().code("VIP20").discountPercent(20).active(false).build()
        );
        couponRepository.saveAll(coupons);
        log.info("Seeded {} coupons", coupons.size());
    }

    private void createDefaultCartItems(User customer) {
        if (customer == null || cartRepository.count() > 0) {
            return;
        }

        String customerId = String.valueOf(customer.getId());
        List<CartItem> cartItems = List.of(
                CartItem.builder().userId(customerId).productId("P001").quantity(1).build(),
                CartItem.builder().userId(customerId).productId("P008").quantity(2).build()
        );
        cartRepository.saveAll(cartItems);
        log.info("Seeded {} cart items", cartItems.size());
    }

    private void createDefaultOrders(User customer) {
        if (customer == null || orderRepository.count() > 0) {
            return;
        }

        String customerId = String.valueOf(customer.getId());
        LocalDateTime now = LocalDateTime.now();

        Order order1 = Order.builder()
                .id("ORD-1001")
                .userId(customerId)
                .shippingFee(29900L)
                .couponCode("WELCOME10")
                .status(OrderStatus.DELIVERED)
                .createdDate(now.minusDays(5))
                .lastModifiedDate(now.minusDays(3))
                .orderItems(new ArrayList<>())
                .build();
        order1.getOrderItems().add(OrderItem.builder().order(order1).productId("P002").quantity(1).price(18990000L).build());
        order1.getOrderItems().add(OrderItem.builder().order(order1).productId("P007").quantity(1).price(7990000L).build());
        order1.setTotalPrice(calculateTotal(order1));

        Order order2 = Order.builder()
                .id("ORD-1002")
                .userId(customerId)
                .shippingFee(29900L)
                .couponCode(null)
                .status(OrderStatus.PENDING)
                .createdDate(now.minusHours(6))
                .lastModifiedDate(now.minusHours(2))
                .orderItems(new ArrayList<>())
                .build();
        order2.getOrderItems().add(OrderItem.builder().order(order2).productId("P003").quantity(1).price(15990000L).build());
        order2.getOrderItems().add(OrderItem.builder().order(order2).productId("P009").quantity(1).price(2790000L).build());
        order2.setTotalPrice(calculateTotal(order2));

        orderRepository.saveAll(List.of(order1, order2));
        log.info("Seeded {} orders with order items", 2);
    }

    private long calculateTotal(Order order) {
        long itemsTotal = order.getOrderItems().stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
        return itemsTotal + (order.getShippingFee() == null ? 0L : order.getShippingFee());
    }
}

// lệnh xóa db
// docker compose exec db psql -U postgres -d shopcart -c "TRUNCATE TABLE order_items, orders, cart_items, inventories, products, coupons, users RESTART IDENTITY CASCADE;"

// chạy lại backend
// docker compose restart backend