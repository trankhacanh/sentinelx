package com.sentinelx.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Lỗi 401/403 phát sinh trong Security filter chain, TRƯỚC khi tới controller,
 * nên @RestControllerAdvice không bắt được. Class này ghi cùng định dạng ErrorResponse.
 */
@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse body = new ErrorResponse(
                Instant.now(), status.value(), code, message, request.getRequestURI(), null);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}