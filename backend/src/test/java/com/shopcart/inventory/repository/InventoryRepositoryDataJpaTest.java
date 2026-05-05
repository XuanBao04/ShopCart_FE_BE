package com.shopcart.inventory.repository;

import com.shopcart.inventory.entity.Inventory;
import com.shopcart.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Inventory Repository — Kiểm thử tích hợp DataJPA")
class InventoryRepositoryDataJpaTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Nên tìm thấy Inventory theo ProductId")
    void findByProductId_Success() {
        Inventory inv = Inventory.builder()
                .productId("P1")
                .quantity(100)
                .reservedQuantity(0)
                .soldQuantity(0)
                .build();
        entityManager.persistAndFlush(inv);

        Optional<Inventory> found = inventoryRepository.findByProductId("P1");

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(100);
    }
}
