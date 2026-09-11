package com.sales.analytics.service;

import com.sales.analytics.dto.SalesSummaryResponse;
import com.sales.analytics.repository.SalesOrderRepository;
import com.sales.analytics.repository.SalesSummaryProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Slf4j
public class AnalyticsService {

    private final SalesOrderRepository salesOrderRepository;

    public AnalyticsService(SalesOrderRepository salesOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
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
}
