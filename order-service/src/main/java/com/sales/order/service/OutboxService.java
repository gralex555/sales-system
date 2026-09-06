package com.sales.order.service;

import tools.jackson.databind.ObjectMapper;
import com.sales.order.entity.OutboxEvent;
import com.sales.order.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void save(Long aggregateId, String eventType, String topic, Object payload) {
            OutboxEvent event = new OutboxEvent();
            event.setAggregateId(aggregateId);
            event.setEventType(eventType);
            event.setTopic(topic);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setCreatedAt(LocalDateTime.now());

            outboxRepository.save(event);
            log.debug("Outbox event saved: type={}, aggregateId={}", eventType, aggregateId);
    }
}
