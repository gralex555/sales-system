package com.sales.analytics.repository;

import com.sales.analytics.entity.SalesItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesItemRepository extends JpaRepository<SalesItem, Long> {

    @Query("SELECT i.productId AS productId, " +
            "i.productName AS productName, " +
            "SUM(i.quantity) AS totalQuantity, " +
            "SUM(i.quantity * i.price) AS totalRevenue " +
            "FROM SalesItem i " +
            "WHERE i.salesOrder.paidAt >= :from AND i.salesOrder.paidAt < :to " +
            "GROUP BY i.productId, i.productName " +
            "ORDER BY SUM(i.quantity * i.price) DESC")
    List<ProductSalesProjection> getTopProducts(@Param("from") LocalDateTime from,
                                                @Param("to") LocalDateTime to,
                                                Pageable pageable);
}
