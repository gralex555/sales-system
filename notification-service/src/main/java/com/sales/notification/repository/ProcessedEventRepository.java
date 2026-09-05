package com.sales.notification.repository;

import com.sales.notification.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    @Modifying
    @Query(value = "INSERT INTO processed_event (event_id, event_type, processed_at) " +
            "VALUES (:eventId, :eventType, :processedAt) " +
            "ON CONFLICT (event_id) DO NOTHING",
            nativeQuery = true)
    int insertIfNotExists(@Param("eventId") String eventId,
                          @Param("eventType") String eventType,
                          @Param("processedAt") LocalDateTime processedAt);
}
