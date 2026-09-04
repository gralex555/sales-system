package com.sales.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_outbox")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Boolean published = false;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime publishedAt;
}
