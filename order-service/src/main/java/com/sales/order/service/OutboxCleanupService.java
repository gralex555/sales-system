package com.sales.order.service;

import com.sales.order.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class OutboxCleanupService {

    private final OutboxEventRepository outboxRepository;
    private final int retentionDays;
    private final int batchSize;

    public OutboxCleanupService(OutboxEventRepository outboxRepository,
                                @Value("${outbox.cleanup.retention-days:7}") int retentionDays,
                                @Value("${outbox.cleanup.batch-size:1000}") int batchSize) {
        this.outboxRepository = outboxRepository;
        this.retentionDays = retentionDays;
        this.batchSize = batchSize;
    }

    @Transactional
    public int deleteBatch() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        return outboxRepository.deletePublishedOlderThan(threshold, batchSize);
    }
}
