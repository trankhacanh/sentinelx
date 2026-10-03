package com.sentinelx.devtools.dto;

import com.sentinelx.common.validation.ValidIpAddress;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GeneratePortScanRequest(
        @NotBlank @ValidIpAddress String sourceIp,

        @Min(1) @Max(1000)
        Integer portCount) {

    public int portCountOrDefault() {
        return portCount != null ? portCount : 20; // mặc định đúng ngưỡng rule
    }
}