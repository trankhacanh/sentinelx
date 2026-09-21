package com.sentinelx.user.dto;

import com.sentinelx.user.entity.User;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

/** Không có passwordHash. Chỉ những gì client cần thấy. */
public record UserResponse(UUID id, String username, String email, String fullName,
                           Set<String> roles, boolean enabled, Instant createdAt) {

    public static UserResponse from(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toCollection(TreeSet::new));
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(),
                user.getFullName(), roleNames, user.isEnabled(), user.getCreatedAt());
    }
}