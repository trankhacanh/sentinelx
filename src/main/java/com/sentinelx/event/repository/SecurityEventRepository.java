package com.sentinelx.event.repository;

import com.sentinelx.event.entity.SecurityEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, UUID>, JpaSpecificationExecutor<SecurityEvent> {
}