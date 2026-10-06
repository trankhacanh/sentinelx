package com.sentinelx.incident.repository;

import com.sentinelx.incident.dto.IncidentFilter;
import com.sentinelx.incident.entity.Incident;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> matching(IncidentFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.severity() != null) {
                predicates.add(cb.equal(root.get("severity"), filter.severity()));
            }
            if (filter.assignedTo() != null) {
                predicates.add(cb.equal(root.get("assignedTo"), filter.assignedTo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}