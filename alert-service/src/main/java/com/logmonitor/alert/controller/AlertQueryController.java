package com.logmonitor.alert.controller;

import com.logmonitor.alert.model.Alert;
import com.logmonitor.alert.repository.AlertRepository;
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
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "alerts", description = "Query alerts raised for ERROR/CRITICAL logs")
@SecurityRequirement(name = "bearerAuth")
public class AlertQueryController {

    private final AlertRepository alertRepository;

    @GetMapping
    @Operation(summary = "List alerts, optionally filtered by serviceName, newest first")
    public Page<Alert> list(
            @RequestParam(required = false) String serviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "triggeredAt"));
        if (serviceName != null) {
            return alertRepository.findByServiceName(serviceName, pageable);
        }
        return alertRepository.findAll(pageable);
    }
}
