package com.sentinelx.audit.repository;

import com.sentinelx.audit.dto.AuditLogFilter;
import com.sentinelx.audit.entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public final class AuditLogSpecifications {
    private AuditLogSpecifications() {}

    public static Specification<AuditLog> matching(AuditLogFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.action() != null) predicates.add(cb.equal(root.get("action"), filter.action()));
            if (filter.userId() != null) predicates.add(cb.equal(root.get("userId"), filter.userId()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}