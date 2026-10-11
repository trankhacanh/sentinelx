package com.sentinelx.audit.dto;

import com.sentinelx.audit.entity.AuditAction;
import java.util.UUID;

public record AuditLogFilter(AuditAction action, UUID userId) {}