package com.logmonitor.ingestion.service;

import com.logmonitor.ingestion.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogPublisherService {

    private final KafkaTemplate<String, LogMessage> kafkaTemplate;

    @Value("${app.kafka.topic:app-logs}")
    private String topic;

    public void publish(LogMessage message) {
        // key by service name so all logs from one service land on the same
        // partition and keep their relative order
        kafkaTemplate.send(topic, message.getServiceName(), message);
        log.info("Published log to topic={} service={} level={}", topic, message.getServiceName(), message.getLevel());
    }
}
