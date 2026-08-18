package com.logmonitor.processing.repository;

import com.logmonitor.processing.model.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
    Page<LogEntry> findByServiceNameAndLevel(String serviceName, String level, Pageable pageable);
    Page<LogEntry> findByServiceName(String serviceName, Pageable pageable);
    Page<LogEntry> findByLevel(String level, Pageable pageable);
}
