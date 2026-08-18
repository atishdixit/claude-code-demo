package com.logmonitor.processing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "logs", indexes = {
        @Index(name = "idx_logs_service_name", columnList = "serviceName"),
        @Index(name = "idx_logs_level", columnList = "level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Instant receivedAt;
}
