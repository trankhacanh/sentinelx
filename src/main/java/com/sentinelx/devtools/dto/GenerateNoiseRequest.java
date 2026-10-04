package com.sentinelx.devtools.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateNoiseRequest(
        @Min(1) @Max(1000)
        Integer count) {

    public int countOrDefault() {
        return count != null ? count : 20;
    }
}