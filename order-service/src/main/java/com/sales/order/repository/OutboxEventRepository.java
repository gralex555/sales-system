package com.sales.order.repository;

import com.sales.order.entity.OutboxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc(Pageable pageable);

    @Query(value = "SELECT * FROM order_outbox WHERE published = false " +
            "ORDER BY created_at LIMIT :limit FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEvent> findUnpublishedForUpdate(@Param("limit") int limit);

    @Modifying
    @Query(value = "DELETE FROM order_outbox WHERE id IN (" +
            "SELECT id FROM order_outbox " +
            "WHERE published = true AND published_at < :threshold " +
            "LIMIT :batchSize)",
            nativeQuery = true)
    int deletePublishedOlderThan(@Param("threshold") LocalDateTime threshold,
                                 @Param("batchSize") int batchSize);

// Подзапрос отбирает порцию, внешний DELETE её удаляет. Возвращает число удалённых строк.
}
