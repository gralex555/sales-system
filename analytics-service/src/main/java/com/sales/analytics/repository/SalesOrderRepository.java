package com.sales.analytics.repository;

import com.sales.analytics.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) AS totalRevenue, " +
            "COUNT(s) AS orderCount, " +
            "COALESCE(AVG(s.totalAmount), 0) AS averageOrderValue " +
            "FROM SalesOrder s WHERE s.paidAt >= :from AND s.paidAt < :to")
    SalesSummaryProjection getSummary(@Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);
}
