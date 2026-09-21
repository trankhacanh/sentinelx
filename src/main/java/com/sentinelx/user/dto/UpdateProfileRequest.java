package com.sentinelx.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 100) String fullName) {
}