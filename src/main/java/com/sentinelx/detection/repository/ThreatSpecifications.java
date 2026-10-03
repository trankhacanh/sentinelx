package com.sentinelx.detection.repository;

import com.sentinelx.common.validation.IpAddresses;
import com.sentinelx.detection.dto.ThreatFilter;
import com.sentinelx.detection.entity.Threat;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class ThreatSpecifications {

    private ThreatSpecifications() {
    }

    public static Specification<Threat> matching(ThreatFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.threatType() != null) {
                predicates.add(cb.equal(root.get("threatType"), filter.threatType()));
            }
            if (filter.severity() != null) {
                predicates.add(cb.equal(root.get("severity"), filter.severity()));
            }
            if (filter.sourceIp() != null && !filter.sourceIp().isBlank()) {
                String ip = filter.sourceIp().trim();
                predicates.add(cb.equal(root.get("sourceIp"),
                        IpAddresses.isValid(ip) ? IpAddresses.normalize(ip) : ip));
            }
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("detectedAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("detectedAt"), filter.to()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}