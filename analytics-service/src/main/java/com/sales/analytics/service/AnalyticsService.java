package com.sales.analytics.service;

import com.sales.analytics.dto.*;
import com.sales.analytics.repository.ProductQuantityProjection;
import com.sales.analytics.repository.SalesItemRepository;
import com.sales.analytics.repository.SalesOrderRepository;
import com.sales.analytics.repository.SalesSummaryProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class AnalyticsService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesItemRepository salesItemRepository;

    public AnalyticsService(SalesOrderRepository salesOrderRepository, SalesItemRepository salesItemRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesItemRepository = salesItemRepository;
    }

    @Cacheable(value = "summary", key = "#from + '-' + #to")
    @Transactional(readOnly = true)
    public SalesSummaryResponse getSummary(LocalDateTime from, LocalDateTime to) {
        log.info("Building sales summary from {} to {}", from, to);

        SalesSummaryProjection summary = salesOrderRepository.getSummary(from, to);

        return new SalesSummaryResponse(
                from, to,
                summary.getTotalRevenue(),
                summary.getOrderCount(),
                summary.getAverageOrderValue().setScale(2, RoundingMode.HALF_UP));
    }


    @Transactional(readOnly = true)
    public List<ProductSalesResponse> getTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        log.info("Building top {} products from {} to {}", limit, from, to);

        return salesItemRepository.getTopProducts(from, to, PageRequest.of(0, limit))
                .stream()
                .map(p -> new ProductSalesResponse(
                        p.getProductId(), p.getProductName(),
                        p.getTotalQuantity(), p.getTotalRevenue()))
                .toList();
    }


    @Transactional(readOnly = true)
    public List<CustomerSalesResponse> getTopCustomers(LocalDateTime from, LocalDateTime to, int limit) {
        log.info("Building top {} customers from {} to {}", limit, from, to);

        return salesOrderRepository.getTopCustomers(from, to, PageRequest.of(0, limit))
                .stream()
                .map(c -> new CustomerSalesResponse(
                        c.getCustomerId(), c.getOrderCount(), c.getTotalRevenue()))
                .toList();
    }

    @Cacheable(value = "productSales", key = "#productId + '-' + #from + '-' + #to")
    @Transactional(readOnly = true)
    public ProductQuantityResponse getProductSales(Long productId, LocalDateTime from, LocalDateTime to) {
        log.info("Building sales for product {} from {} to {}", productId, from, to);

        ProductQuantityProjection sales = salesItemRepository.getProductSales(productId, from, to);

        return new ProductQuantityResponse(
                productId, from, to,
                sales.getTotalQuantity(), sales.getTotalRevenue());
    }

    @Transactional(readOnly = true)
    public SalesComparisonResponse compare(LocalDateTime currentFrom, LocalDateTime currentTo,
                                           LocalDateTime previousFrom, LocalDateTime previousTo) {
        log.info("Comparing period {}..{} with {}..{}", currentFrom, currentTo, previousFrom, previousTo);

        SalesSummaryResponse current = getSummary(currentFrom, currentTo);
        SalesSummaryResponse previous = getSummary(previousFrom, previousTo);

        BigDecimal revenueChange = calculateChangePercent(
                previous.getTotalRevenue(), current.getTotalRevenue());

        BigDecimal orderCountChange = calculateChangePercent(
                BigDecimal.valueOf(previous.getOrderCount()),
                BigDecimal.valueOf(current.getOrderCount()));

        return new SalesComparisonResponse(current, previous, revenueChange, orderCountChange);
    }

    private BigDecimal calculateChangePercent(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return newValue.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(100);
        }

        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
