package com.sales.notification.service;


import com.sales.notification.repository.ProcessedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ProcessedEventCleanupService {

    private final ProcessedEventRepository eventRepository;
    private final int retentionDays;
    private final int batchSize;

    public ProcessedEventCleanupService(ProcessedEventRepository eventRepository,
                                        @Value("${processed-event.cleanup.retention-days:7}") int retentionDays,
                                        @Value("${processed-event.cleanup.batch-size:1000}") int batchSize) {
        this.eventRepository = eventRepository;
        this.retentionDays = retentionDays;
        this.batchSize = batchSize;
    }

    @Transactional
    public int deleteBatch() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        return eventRepository.deleteOlderThan(threshold, batchSize);
    }
}
