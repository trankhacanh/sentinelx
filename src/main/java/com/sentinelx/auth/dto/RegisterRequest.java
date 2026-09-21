package com.sentinelx.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$",
                message = "Username must be 3-50 characters: letters, digits, '.', '_' or '-'")
        String username,

        @NotBlank @Email @Size(max = 255)
        String email,

        // BCrypt chỉ xử lý tối đa 72 byte đầu, nên giới hạn 72
        @NotBlank
        @Size(min = 12, max = 72, message = "Password must be 12-72 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Password must contain at least one letter and one digit")
        String password,

        @Size(max = 100)
        String fullName) {

    /** Ngăn password lọt vào log nếu ai đó vô tình log object này. */
    @Override
    public String toString() {
        return "RegisterRequest[username=" + username + ", email=" + email + "]";
    }
}