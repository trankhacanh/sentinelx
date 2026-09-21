package com.sentinelx.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Trả 403 khi đã đăng nhập nhưng không đủ quyền (rule cấp URL). */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorWriter errorWriter;

    public RestAccessDeniedHandler(SecurityErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        errorWriter.write(request, response, HttpStatus.FORBIDDEN, "FORBIDDEN",
                "You do not have permission to perform this action");
    }
}