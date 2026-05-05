package com.shopcart.cart.repository;

import com.shopcart.cart.entity.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.shopcart.config.database.JpaConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("Cart Repository — Kiểm thử tích hợp DataJPA")
class CartRepositoryDataJpaTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final String userId = "user-123";
    private final String productId = "prod-456";

    @BeforeEach
    void setUp() {
        
    }

    @Test
    @DisplayName("Nên tìm thấy CartItem theo UserId và ProductId")
    void findByUserIdAndProductId_Success() {
        CartItem item = CartItem.builder()
                .userId(userId)
                .productId(productId)
                .quantity(2)
                .build();
        entityManager.persistAndFlush(item);

        Optional<CartItem> found = cartRepository.findByUserIdAndProductId(userId, productId);

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Nên xóa được toàn bộ sản phẩm trong giỏ của User")
    void deleteByUserId_Success() {
        CartItem item1 = CartItem.builder().userId(userId).productId("p1").quantity(1).build();
        CartItem item2 = CartItem.builder().userId(userId).productId("p2").quantity(2).build();
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        cartRepository.deleteByUserId(userId);
        entityManager.clear(); // Clear persistence context to see effects

        List<CartItem> remaining = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        assertThat(remaining).isEmpty();
    }

    @Test
    @DisplayName("Nên lấy danh sách sản phẩm theo thứ tự thời gian giảm dần")
    void findByUserIdOrderByCreatedAtDesc_Success() throws InterruptedException {
        CartItem item1 = CartItem.builder().userId(userId).productId("p1").quantity(1).build();
        entityManager.persistAndFlush(item1);
        
        Thread.sleep(10); // Đảm bảo thời gian tạo khác nhau

        CartItem item2 = CartItem.builder().userId(userId).productId("p2").quantity(2).build();
        entityManager.persistAndFlush(item2);

        List<CartItem> items = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getProductId()).isEqualTo("p2"); 
        assertThat(items.get(1).getProductId()).isEqualTo("p1");
    }

    @Test
    @DisplayName("Nên tự động điền thông tin ngày tạo (createdAt)")
    void testAuditingFields() {
        CartItem item = CartItem.builder()
                .userId(userId)
                .productId(productId)
                .quantity(1)
                .build();
        
        CartItem saved = cartRepository.save(item);
        entityManager.flush();

        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
