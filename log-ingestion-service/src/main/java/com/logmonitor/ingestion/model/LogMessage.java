package com.logmonitor.ingestion.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** The JSON shape published to the Kafka "app-logs" topic. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String serviceName;
    private String level;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
