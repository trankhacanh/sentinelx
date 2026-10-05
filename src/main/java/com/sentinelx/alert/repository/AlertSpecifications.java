package com.sentinelx.alert.repository;

import com.sentinelx.alert.dto.AlertFilter;
import com.sentinelx.alert.entity.Alert;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class AlertSpecifications {

    private AlertSpecifications() {
    }

    public static Specification<Alert> matching(AlertFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.assignedTo() != null) {
                predicates.add(cb.equal(root.get("assignedTo"), filter.assignedTo()));
            }
            if (filter.severity() != null) {
                // severity thuộc Threat liên kết, không thuộc Alert -> cần join
                var threatJoin = root.join("threat");
                predicates.add(cb.equal(threatJoin.get("severity"), filter.severity()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}