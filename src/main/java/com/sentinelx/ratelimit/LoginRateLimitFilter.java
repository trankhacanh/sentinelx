package com.sentinelx.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelx.common.exception.TooManyRequestsException;
import com.sentinelx.common.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Chạy ở tầng servlet filter, TRƯỚC Spring Security filter chain (xem SecurityConfig: đăng ký
 * filter này bằng addFilterBefore). Rate limiting là mối quan tâm hạ tầng, tách biệt khỏi logic
 * nghiệp vụ xác thực trong AuthService (xem quyết định kiến trúc 2g).
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/auth/login";

    private final LoginRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public LoginRateLimitFilter(LoginRateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!LOGIN_PATH.equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        try {
            rateLimiter.checkAndIncrement(request.getRemoteAddr());
            chain.doFilter(request, response);
        } catch (TooManyRequestsException ex) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(ex.getRetryAfter().toSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.TOO_MANY_REQUESTS.value(),
                    "TOO_MANY_REQUESTS", ex.getMessage(), request.getRequestURI(), null);
            objectMapper.writeValue(response.getOutputStream(), body);
        }
    }
}