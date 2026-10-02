package com.sales.analytics.service;

import com.sales.analytics.dto.ProductSalesResponse;
import com.sales.analytics.entity.SalesItem;
import com.sales.analytics.entity.SalesOrder;
import com.sales.analytics.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class AnalyticsServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @BeforeEach
    void cleanUp() {
        salesOrderRepository.deleteAll();
    }

    private void addItem(SalesOrder order, Long productId, String name, int qty, BigDecimal price) {
        SalesItem item = new SalesItem();
        item.setProductId(productId);
        item.setProductName(name);
        item.setQuantity(qty);
        item.setPrice(price);
        item.setSalesOrder(order);
        order.getItems().add(item);
    }

    @Test
    void shouldCalculateTopProductsByRevenue() {
        // given
        LocalDateTime now = LocalDateTime.now();

        SalesOrder order1 = new SalesOrder();
        order1.setOrderId(1L);
        order1.setCustomerId(42L);
        order1.setTotalAmount(BigDecimal.valueOf(900));
        order1.setPaidAt(now.minusDays(1));
        addItem(order1, 1L, "Цемент", 2, BigDecimal.valueOf(450));
        salesOrderRepository.save(order1);

        SalesOrder order2 = new SalesOrder();
        order2.setOrderId(2L);
        order2.setCustomerId(77L);
        order2.setTotalAmount(BigDecimal.valueOf(2240));
        order2.setPaidAt(now.minusDays(1));
        addItem(order2, 1L, "Цемент", 3, BigDecimal.valueOf(450));
        addItem(order2, 2L, "Штукатурка", 1, BigDecimal.valueOf(890));
        salesOrderRepository.save(order2);

        // when
        List<ProductSalesResponse> top =
                analyticsService.getTopProducts(now.minusDays(7), now.plusDays(1), 10);

        // then
        assertThat(top).hasSize(2);

        assertThat(top.get(0).getProductName()).isEqualTo("Цемент");
        assertThat(top.get(0).getTotalQuantity()).isEqualTo(5);
        assertThat(top.get(0).getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(2250));

        assertThat(top.get(1).getProductName()).isEqualTo("Штукатурка");
        assertThat(top.get(1).getTotalQuantity()).isEqualTo(1);
        assertThat(top.get(1).getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(890));
    }
}
