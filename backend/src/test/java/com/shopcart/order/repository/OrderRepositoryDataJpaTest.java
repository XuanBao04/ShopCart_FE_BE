package com.shopcart.order.repository;

import com.shopcart.order.entity.Order;
import com.shopcart.order.entity.OrderItem;
import com.shopcart.common.enums.OrderStatus;
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
@DisplayName("Order Repository — Kiểm thử tích hợp DataJPA")
class OrderRepositoryDataJpaTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final String userId = "user-123";

    @Test
    @DisplayName("Nên lưu đơn hàng cùng với các OrderItems (Cascade)")
    void saveOrderWithItems_Success() {
        Order order = Order.builder()
                .id("ORDER-1")
                .userId(userId)
                .totalPrice(200000L)
                .status(OrderStatus.PENDING)
                .build();

        OrderItem item1 = OrderItem.builder().productId("p1").quantity(1).price(100000L).order(order).build();
        OrderItem item2 = OrderItem.builder().productId("p2").quantity(1).price(100000L).order(order).build();
        order.setOrderItems(List.of(item1, item2));

        orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> found = orderRepository.findById("ORDER-1");
        assertThat(found).isPresent();
        assertThat(found.get().getOrderItems()).hasSize(2);
        assertThat(found.get().getOrderItems().get(0).getOrder()).isNotNull();
    }

    @Test
    @DisplayName("Nên lấy danh sách đơn hàng của User sắp xếp theo thời gian")
    void findByUserIdOrderByCreatedAtDesc_Success() throws InterruptedException {
        Order o1 = Order.builder().id("O1").userId(userId).totalPrice(100L).status(OrderStatus.PENDING).build();
        orderRepository.save(o1);
        entityManager.flush();
        
        Thread.sleep(10);

        Order o2 = Order.builder().id("O2").userId(userId).totalPrice(200L).status(OrderStatus.PENDING).build();
        orderRepository.save(o2);
        entityManager.flush();

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isEqualTo("O2"); // Latest first
    }
}

// mvn test -Dtest=*RepositoryDataJpaTest
