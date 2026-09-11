package com.sales.analytics.controller;

import com.sales.analytics.dto.ProductSalesResponse;
import com.sales.analytics.dto.SalesSummaryResponse;
import com.sales.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Sales summary for period",
            description = "Revenue, order count and average order value")
    @ApiResponse(responseCode = "200", description = "Summary calculated")
    @GetMapping("/summary")
    public ResponseEntity<SalesSummaryResponse> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return ResponseEntity.ok(analyticsService.getSummary(from, to));
    }

    @Operation(summary = "Top products by revenue")
    @GetMapping("/top-products")
    public ResponseEntity<List<ProductSalesResponse>> getTopProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(analyticsService.getTopProducts(from, to, limit));
    }
}
