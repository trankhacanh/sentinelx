package com.sentinelx.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/**
 * Định dạng lỗi thống nhất, khớp đặc tả:
 * timestamp / status / error / message / path.
 * "details" chỉ xuất hiện khi có lỗi validation theo từng field.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> details) {

    public record FieldViolation(String field, String message) {
    }
}