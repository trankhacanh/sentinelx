package com.sentinelx.alert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAlertTitleRequest(@NotBlank @Size(max = 200) String title) {
}