package com.logmonitor.alert.kafka;

import com.logmonitor.alert.model.Alert;
import com.logmonitor.alert.model.LogMessage;
import com.logmonitor.alert.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private static final Set<String> ALERT_LEVELS = Set.of("ERROR", "CRITICAL");

    private final AlertRepository alertRepository;

    @KafkaListener(topics = "${app.kafka.topic:app-logs}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(LogMessage message) {
        if (!ALERT_LEVELS.contains(message.getLevel())) {
            return;
        }

        Alert alert = Alert.builder()
                .serviceName(message.getServiceName())
                .level(message.getLevel())
                .message(message.getMessage())
                .timestamp(message.getTimestamp())
                .triggeredAt(Instant.now())
                .build();
        alertRepository.save(alert);

        // In a real system this is where you'd call Slack/PagerDuty/email.
        log.warn("ALERT [{}] service={} message={}", alert.getLevel(), alert.getServiceName(), alert.getMessage());
    }
}
