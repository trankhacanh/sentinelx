package com.sentinelx.audit.service;

import com.sentinelx.audit.entity.AuditAction;
import com.sentinelx.audit.entity.AuditLog;
import com.sentinelx.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * REQUIRES_NEW: ghi audit log trong transaction RIÊNG để lỗi ghi log (hiếm gặp) không
     * làm rollback hành động nghiệp vụ chính nó đang ghi lại. Khác với DetectionRuleEngine
     * (Phase 4/8) -- ở đó Threat PHẢI cùng transaction với Event vì có FK ràng buộc chặt; ở đây
     * AuditLog không có FK bắt buộc phải tồn tại cùng lúc với resource, nên tách an toàn.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, AuditAction action, String resourceType, UUID resourceId,
                       String ipAddress, Map<String, Object> metadata) {
        try {
            repository.save(new AuditLog(userId, action, resourceType, resourceId, ipAddress, metadata));
        } catch (Exception ex) {
            log.error("Failed to write audit log for action {}: {}", action, ex.getMessage(), ex);
        }
    }
}