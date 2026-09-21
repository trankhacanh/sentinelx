package com.sentinelx.common.response;

import java.util.List;
import org.springframework.data.domain.Page;

/** Ổn định hơn việc serialize thẳng Page của Spring Data. Sẽ dùng lại ở Phase 3. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}