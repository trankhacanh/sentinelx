package com.sentinelx.detection.repository;

import com.sentinelx.detection.entity.Threat;
import com.sentinelx.detection.entity.ThreatType;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreatRepository extends JpaRepository<Threat, UUID> {

    /** Dùng để chặn tạo threat trùng lặp trong cùng một cửa sổ đang diễn ra (xem quyết định 2d). */
    boolean existsByThreatTypeAndSourceIpAndDetectedAtBetween(
            ThreatType threatType, String sourceIp, Instant from, Instant to);
}