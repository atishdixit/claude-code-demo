package com.logmonitor.ingestion.controller;

import com.logmonitor.ingestion.dto.LogEntryRequest;
import com.logmonitor.ingestion.model.LogMessage;
import com.logmonitor.ingestion.service.LogPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "logs", description = "Submit log entries (published to Kafka)")
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final LogPublisherService logPublisherService;

    @PostMapping
    @Operation(summary = "Publish a log entry to the app-logs Kafka topic")
    public ResponseEntity<Map<String, Object>> submit(@Valid @RequestBody LogEntryRequest request) {
        LogMessage message = new LogMessage(
                request.getServiceName(),
                request.getLevel(),
                request.getMessage(),
                Instant.now()
        );
        logPublisherService.publish(message);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "status", "published",
                "serviceName", message.getServiceName(),
                "level", message.getLevel(),
                "timestamp", message.getTimestamp().toString()
        ));
    }
}
