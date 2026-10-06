package com.sentinelx.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.common.exception.BadRequestException;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.common.validation.IpAddresses;
import com.sentinelx.event.dto.CreateSecurityEventRequest;
import com.sentinelx.event.dto.EventFilter;
import com.sentinelx.event.dto.SecurityEventResponse;
import com.sentinelx.event.entity.SecurityEvent;
import com.sentinelx.event.repository.SecurityEventRepository;
import com.sentinelx.event.repository.SecurityEventSpecifications;
import com.sentinelx.messaging.EventIngestedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SecurityEventService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SecurityEventRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final EventIngestionProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    public SecurityEventService(SecurityEventRepository repository, ObjectMapper objectMapper,
                                Clock clock, EventIngestionProperties properties,
                                ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.properties = properties;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SecurityEventResponse ingest(CreateSecurityEventRequest request) {
        Instant latestAllowed = clock.instant().plus(properties.maxFutureSkew());
        if (request.timestamp().isAfter(latestAllowed)) {
            throw new BadRequestException("Event timestamp is too far in the future");
        }
        validatePayloadSize(request.payload());

        SecurityEvent event = new SecurityEvent(
                request.timestamp(),
                request.eventType(),
                request.source().trim(),
                IpAddresses.normalize(request.sourceIp()),
                request.destinationIp() == null ? null : IpAddresses.normalize(request.destinationIp()),
                normalizeUsername(request.username()),
                request.severity(),
                request.payload());

        SecurityEvent saved = repository.save(event);

        // Phase 8: KHÔNG gọi DetectionRuleEngine trực tiếp (đồng bộ) nữa. Thay vào đó phát ra một
        // Spring application event nội bộ; EventDetectionPublisher (package messaging) lắng nghe
        // ở pha AFTER_COMMIT và CHỈ KHI ĐÓ mới gửi message vào RabbitMQ. Nếu transaction này
        // rollback vì bất kỳ lý do gì sau dòng này, message sẽ KHÔNG BAO GIỜ được gửi đi.
        eventPublisher.publishEvent(new EventIngestedEvent(saved.getId()));

        return SecurityEventResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SecurityEventResponse getById(UUID id) {
        return repository.findById(id)
                .map(SecurityEventResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Security event", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<SecurityEventResponse> list(EventFilter filter, int page, int size) {
        validateTimeRange(filter);
        return query(SecurityEventSpecifications.matching(filter), page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<SecurityEventResponse> search(String text, EventFilter filter, int page, int size) {
        validateTimeRange(filter);
        Specification<SecurityEvent> spec = SecurityEventSpecifications.matching(filter)
                .and(SecurityEventSpecifications.containsText(text));
        return query(spec, page, size);
    }

    private PageResponse<SecurityEventResponse> query(Specification<SecurityEvent> spec, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.desc("timestamp"), Sort.Order.desc("id")));
        return PageResponse.from(repository.findAll(spec, pageable).map(SecurityEventResponse::from));
    }

    private void validateTimeRange(EventFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
    }

    private void validatePayloadSize(Map<String, Object> payload) {
        if (payload == null) {
            return;
        }
        try {
            if (objectMapper.writeValueAsBytes(payload).length > properties.maxPayloadBytes()) {
                throw new BadRequestException(
                        "Payload exceeds the maximum size of " + properties.maxPayloadBytes() + " bytes");
            }
        } catch (JsonProcessingException ex) {
            throw new BadRequestException("Payload cannot be processed");
        }
    }

    private static String normalizeUsername(String username) {
        return (username == null || username.isBlank()) ? null : username.trim().toLowerCase(Locale.ROOT);
    }
}