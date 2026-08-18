package com.logmonitor.alert.repository;

import com.logmonitor.alert.model.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Page<Alert> findByServiceName(String serviceName, Pageable pageable);
}
