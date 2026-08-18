package com.logmonitor.processing.controller;

import com.logmonitor.processing.model.LogEntry;
import com.logmonitor.processing.repository.LogEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "logs", description = "Query persisted logs")
@SecurityRequirement(name = "bearerAuth")
public class LogQueryController {

    private final LogEntryRepository logEntryRepository;

    @GetMapping
    @Operation(summary = "List logs, optionally filtered by serviceName and/or level, newest first")
    public Page<LogEntry> list(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        if (serviceName != null && level != null) {
            return logEntryRepository.findByServiceNameAndLevel(serviceName, level, pageable);
        } else if (serviceName != null) {
            return logEntryRepository.findByServiceName(serviceName, pageable);
        } else if (level != null) {
            return logEntryRepository.findByLevel(level, pageable);
        }
        return logEntryRepository.findAll(pageable);
    }
}
