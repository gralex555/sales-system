package com.sales.analytics.repository;

import com.sales.analytics.entity.SalesOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) AS totalRevenue, " +
            "COUNT(s) AS orderCount, " +
            "COALESCE(AVG(s.totalAmount), 0) AS averageOrderValue " +
            "FROM SalesOrder s WHERE s.paidAt >= :from AND s.paidAt < :to")
    SalesSummaryProjection getSummary(@Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);


    @Query("SELECT s.customerId AS customerId, " +
            "COUNT(s) AS orderCount, " +
            "SUM(s.totalAmount) AS totalRevenue " +
            "FROM SalesOrder s " +
            "WHERE s.paidAt >= :from AND s.paidAt < :to " +
            "GROUP BY s.customerId " +
            "ORDER BY SUM(s.totalAmount) DESC")
    List<CustomerSalesProjection> getTopCustomers(@Param("from") LocalDateTime from,
                                                  @Param("to") LocalDateTime to,
                                                  Pageable pageable);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) AS totalQuantity, " +
            "COALESCE(SUM(i.quantity * i.price), 0) AS totalRevenue " +
            "FROM SalesItem i " +
            "WHERE i.productId = :productId " +
            "AND i.salesOrder.paidAt >= :from AND i.salesOrder.paidAt < :to")
    ProductQuantityProjection getProductSales(@Param("productId") Long productId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
}
