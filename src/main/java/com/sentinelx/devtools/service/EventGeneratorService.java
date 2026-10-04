package com.sentinelx.devtools.service;

import com.sentinelx.common.model.Severity;
import com.sentinelx.devtools.dto.GenerateBruteForceRequest;
import com.sentinelx.devtools.dto.GenerateNoiseRequest;
import com.sentinelx.devtools.dto.GeneratePortScanRequest;
import com.sentinelx.devtools.dto.GenerationResult;
import com.sentinelx.event.dto.CreateSecurityEventRequest;
import com.sentinelx.event.entity.EventType;
import com.sentinelx.event.service.SecurityEventService;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class EventGeneratorService {

    private static final int STEP_MILLIS = 10;

    private final SecurityEventService eventService;
    private final Clock clock;

    public EventGeneratorService(SecurityEventService eventService, Clock clock) {
        this.eventService = eventService;
        this.clock = clock;
    }

    public GenerationResult generateBruteForce(GenerateBruteForceRequest request) {
        int count = request.countOrDefault();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var created = eventService.ingest(new CreateSecurityEventRequest(
                    monotonicTimestamp(i, count),
                    EventType.LOGIN_FAILED,
                    "EVENT_GENERATOR",
                    request.sourceIp(),
                    null,
                    request.usernameOrDefault(),
                    Severity.MEDIUM,
                    null));
            ids.add(created.id());
        }
        return new GenerationResult(ids.size(), ids);
    }

    public GenerationResult generatePortScan(GeneratePortScanRequest request) {
        int count = request.portCountOrDefault();
        List<UUID> ids = new ArrayList<>();
        int startPort = 1024;
        for (int i = 0; i < count; i++) {
            var created = eventService.ingest(new CreateSecurityEventRequest(
                    monotonicTimestamp(i, count),
                    EventType.PORT_SCAN,
                    "EVENT_GENERATOR",
                    request.sourceIp(),
                    null,
                    null,
                    Severity.HIGH,
                    Map.of("destinationPort", startPort + i)));
            ids.add(created.id());
        }
        return new GenerationResult(ids.size(), ids);
    }

    public GenerationResult generateNoise(GenerateNoiseRequest request) {
        int count = request.countOrDefault();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var created = eventService.ingest(new CreateSecurityEventRequest(
                    monotonicTimestamp(i, count),
                    randomNoiseEventType(),
                    "EVENT_GENERATOR",
                    randomPrivateIp(),
                    null,
                    "user" + ThreadLocalRandom.current().nextInt(1, 50),
                    Severity.LOW,
                    Map.of("generated", true)));
            ids.add(created.id());
        }
        return new GenerationResult(ids.size(), ids);
    }

    /**
     * Timestamp tăng dần đều theo đúng thứ tự vòng lặp (i=0 sớm nhất, i=count-1 = đúng thời điểm
     * hiện tại). Bản trước dùng jitter NGẪU NHIÊN khiến thứ tự timestamp không khớp thứ tự gửi
     * thực tế — event gửi SAU có thể có timestamp SỚM HƠN event gửi trước đó. Vì các detector
     * (PortScanDetector, BruteForceDetector) tính cửa sổ dựa trên event.getTimestamp() của CHÍNH
     * event đang xét, một timestamp "lùi" khiến event đã commit trước đó (nhưng có timestamp
     * muộn hơn) bị loại khỏi phép đếm, làm đếm THIẾU và có thể không bao giờ đạt ngưỡng dù đã gửi
     * đủ số lượng. Tăng dần đơn điệu mô phỏng đúng cách event thật xảy ra tuần tự theo thời gian.
     */
    private Instant monotonicTimestamp(int index, int total) {
        long millisBeforeNow = (long) (total - 1 - index) * STEP_MILLIS;
        return clock.instant().minusMillis(millisBeforeNow);
    }

    private EventType randomNoiseEventType() {
        return ThreadLocalRandom.current().nextBoolean() ? EventType.LOGIN_SUCCESS : EventType.HTTP_REQUEST;
    }

    private String randomPrivateIp() {
        return "10.99." + ThreadLocalRandom.current().nextInt(0, 255) + "."
                + ThreadLocalRandom.current().nextInt(1, 255);
    }
}