package com.sentinelx.detection.repository;

import com.sentinelx.detection.dto.SourceIpCount;
import com.sentinelx.detection.entity.Threat;
import com.sentinelx.detection.entity.ThreatType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThreatRepository extends JpaRepository<Threat, UUID>, JpaSpecificationExecutor<Threat> {

    boolean existsByThreatTypeAndSourceIpAndDetectedAtBetween(
            ThreatType threatType, String sourceIp, Instant from, Instant to);

    @Query("""
            SELECT t.severity AS label, COUNT(t) AS cnt
            FROM Threat t
            WHERE t.detectedAt BETWEEN :from AND :to
            GROUP BY t.severity
            """)
    List<LabelCount> countBySeverity(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT t.threatType AS label, COUNT(t) AS cnt
            FROM Threat t
            WHERE t.detectedAt BETWEEN :from AND :to
            GROUP BY t.threatType
            """)
    List<LabelCount> countByThreatType(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT t.sourceIp AS sourceIp, COUNT(t) AS count
            FROM Threat t
            WHERE t.detectedAt BETWEEN :from AND :to
            GROUP BY t.sourceIp
            ORDER BY COUNT(t) DESC
            """)
    List<SourceIpCount> topSourceIps(@Param("from") Instant from, @Param("to") Instant to,
                                     org.springframework.data.domain.Pageable pageable);

    long countByDetectedAtBetween(Instant from, Instant to);

    /** Projection interface cho 2 query group-by ở trên; enum trả về dạng tên qua label.toString(). */
    interface LabelCount {
        Object getLabel();

        long getCnt();
    }
}