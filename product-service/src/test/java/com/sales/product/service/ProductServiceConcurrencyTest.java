package com.sales.product.service;
import com.sales.product.dto.CreateProductRequest;
import com.sales.product.dto.ProductResponse;
import com.sales.product.entity.ReservationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class ProductServiceConcurrencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductService productService;

    @Test
    void shouldReserveOnlyOnceWhenTwoThreadsCompeteForLastItem() throws InterruptedException {
        // arrange: товар в ЕДИНСТВЕННОМ экземпляре
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Последний мешок");
        request.setPrice(BigDecimal.valueOf(500));
        request.setQuantityAvailable(1);

        ProductResponse product = productService.createProduct(request);
        Long productId = product.getId();

        // act: два потока одновременно резервируют по 1 штуке
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();                     // ждём общего старта
                    ReservationResult result = productService.reserveStock(productId, 1);
                    if (result == ReservationResult.SUCCESS) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        startLatch.countDown();                              // старт одновременно!
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // assert
        assertThat(successCount.get()).isEqualTo(1);         // ровно один успел
        assertThat(failureCount.get()).isEqualTo(1);         // второму не хватило

        ProductResponse after = productService.getProductById(productId);
        assertThat(after.getQuantityAvailable()).isZero();
        assertThat(after.getQuantityReserved()).isEqualTo(1);
    }
}
