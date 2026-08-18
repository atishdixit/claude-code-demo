package com.logmonitor.alert.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alerts_service_name", columnList = "serviceName"),
        @Index(name = "idx_alerts_level", columnList = "level")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

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
    private Instant triggeredAt;
}
