package com.logmonitor.processing.kafka;

import com.logmonitor.processing.model.LogEntry;
import com.logmonitor.processing.model.LogMessage;
import com.logmonitor.processing.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogConsumer {

    private final LogEntryRepository logEntryRepository;

    @KafkaListener(topics = "${app.kafka.topic:app-logs}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(LogMessage message) {
        LogEntry entry = LogEntry.builder()
                .serviceName(message.getServiceName())
                .level(message.getLevel())
                .message(message.getMessage())
                .timestamp(message.getTimestamp())
                .receivedAt(Instant.now())
                .build();
        logEntryRepository.save(entry);
        log.info("Persisted log id={} service={} level={}", entry.getId(), entry.getServiceName(), entry.getLevel());
    }
}
