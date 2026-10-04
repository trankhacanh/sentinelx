package com.sentinelx.devtools.dto;

import com.sentinelx.common.validation.ValidIpAddress;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GenerateBruteForceRequest(
        @NotBlank @ValidIpAddress String sourceIp,

        @Min(1) @Max(1000)
        Integer count,

        String username) {

    public int countOrDefault() {
        return count != null ? count : 7; // mặc định 7, khớp kịch bản mục 29 của đặc tả
    }

    public String usernameOrDefault() {
        return (username == null || username.isBlank()) ? "admin" : username;
    }
}