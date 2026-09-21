package com.sentinelx.auth.dto;

import com.sentinelx.user.dto.UserResponse;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {
}