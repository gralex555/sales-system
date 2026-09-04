package com.sales.order.repository;

import com.sales.order.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc(Pageable pageable);

    @Query(value = "SELECT * FROM order_outbox WHERE published = false " +
            "ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEvent> findUnpublishedForUpdate(@Param("limit") int limit);
}
