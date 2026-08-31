package com.sales.order.repository;

import com.sales.order.entity.Order;
import com.sales.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findAll(Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items " +
            "WHERE o.status = :status AND o.reservedUntil < :now")
    List<Order> findExpiredOrders(@Param("status") OrderStatus status,
                                  @Param("now") LocalDateTime now,
                                  Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus, o.updatedAt = :now " +
            "WHERE o.id = :id AND o.status = :expectedStatus")
    int cancelIfStillCreated(@Param("id") Long id,
                             @Param("newStatus") OrderStatus newStatus,
                             @Param("expectedStatus") OrderStatus expectedStatus,
                             @Param("now") LocalDateTime now);

}
