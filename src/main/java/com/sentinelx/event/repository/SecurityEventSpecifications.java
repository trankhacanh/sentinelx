package com.sentinelx.event.repository;

import com.sentinelx.common.validation.IpAddresses;
import com.sentinelx.event.dto.EventFilter;
import com.sentinelx.event.entity.SecurityEvent;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification xây câu WHERE động, chỉ thêm điều kiện cho tham số có giá trị.
 * Mọi giá trị đi qua tham số (bind parameter) nên không có nguy cơ SQL injection.
 */
public final class SecurityEventSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private SecurityEventSpecifications() {
    }

    public static Specification<SecurityEvent> matching(EventFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.eventType() != null) {
                predicates.add(cb.equal(root.get("eventType"), filter.eventType()));
            }
            if (filter.severity() != null) {
                predicates.add(cb.equal(root.get("severity"), filter.severity()));
            }
            if (hasText(filter.sourceIp())) {
                String ip = filter.sourceIp().trim();
                predicates.add(cb.equal(root.get("sourceIp"),
                        IpAddresses.isValid(ip) ? IpAddresses.normalize(ip) : ip));
            }
            if (hasText(filter.username())) {
                predicates.add(cb.equal(root.get("username"),
                        filter.username().trim().toLowerCase(Locale.ROOT)));
            }
            if (filter.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), filter.to()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Tìm chuỗi con (không phân biệt hoa thường) trên cột search_text được index bằng trigram. */
    public static Specification<SecurityEvent> containsText(String text) {
        String pattern = "%" + escapeLike(text.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(root.get("searchText"), pattern, LIKE_ESCAPE);
    }

    /** Người dùng gõ '%' hoặc '_' phải được hiểu là ký tự thường, không phải wildcard. */
    private static String escapeLike(String raw) {
        return raw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}