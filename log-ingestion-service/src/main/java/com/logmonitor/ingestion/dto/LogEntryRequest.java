package com.logmonitor.ingestion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LogEntryRequest {

    @NotBlank
    @Schema(example = "order-service")
    private String serviceName;

    @NotBlank
    @Pattern(regexp = "INFO|WARN|ERROR|CRITICAL", message = "level must be INFO, WARN, ERROR, or CRITICAL")
    @Schema(example = "ERROR")
    private String level;

    @NotBlank
    @Schema(example = "Failed to charge payment for order #4821")
    private String message;
}
