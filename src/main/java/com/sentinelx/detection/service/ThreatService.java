package com.sentinelx.detection.service;

import com.sentinelx.common.exception.BadRequestException;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.detection.dto.ThreatFilter;
import com.sentinelx.detection.dto.ThreatResponse;
import com.sentinelx.detection.dto.ThreatStatisticsResponse;
import com.sentinelx.detection.repository.ThreatRepository;
import com.sentinelx.detection.repository.ThreatSpecifications;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThreatService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Duration DEFAULT_STATISTICS_WINDOW = Duration.ofHours(24);
    private static final int TOP_IP_LIMIT = 10;

    private final ThreatRepository repository;
    private final Clock clock;

    public ThreatService(ThreatRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ThreatResponse getById(UUID id) {
        return repository.findById(id)
                .map(ThreatResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Threat", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ThreatResponse> list(ThreatFilter filter, int page, int size) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.desc("detectedAt"), Sort.Order.desc("id")));
        return PageResponse.from(
                repository.findAll(ThreatSpecifications.matching(filter), pageable).map(ThreatResponse::from));
    }

    @Transactional(readOnly = true)
    public ThreatStatisticsResponse statistics(Instant from, Instant to) {
        Instant effectiveTo = (to != null) ? to : clock.instant();
        Instant effectiveFrom = (from != null) ? from : effectiveTo.minus(DEFAULT_STATISTICS_WINDOW);
        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }

        Map<String, Long> bySeverity = new HashMap<>();
        repository.countBySeverity(effectiveFrom, effectiveTo)
                .forEach(row -> bySeverity.put(row.getLabel().toString(), row.getCnt()));

        Map<String, Long> byType = new HashMap<>();
        repository.countByThreatType(effectiveFrom, effectiveTo)
                .forEach(row -> byType.put(row.getLabel().toString(), row.getCnt()));

        var topIps = repository.topSourceIps(effectiveFrom, effectiveTo, PageRequest.of(0, TOP_IP_LIMIT));
        long total = repository.countByDetectedAtBetween(effectiveFrom, effectiveTo);

        return new ThreatStatisticsResponse(effectiveFrom, effectiveTo, total, bySeverity, byType, topIps);
    }
}