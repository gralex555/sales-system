package com.sales.analytics.service;

import com.sales.analytics.dto.ProductSalesResponse;
import com.sales.analytics.dto.SalesSummaryResponse;
import com.sales.analytics.repository.SalesItemRepository;
import com.sales.analytics.repository.SalesOrderRepository;
import com.sales.analytics.repository.SalesSummaryProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
