package com.sentinelx.detection.repository;

import com.sentinelx.detection.entity.Threat;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Thêm query method khi Bước 4.2/4.3 cần (ví dụ đếm threat theo IP). */
public interface ThreatRepository extends JpaRepository<Threat, UUID> {
}