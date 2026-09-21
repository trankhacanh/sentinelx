package com.sentinelx.user.dto;

import com.sentinelx.user.entity.RoleName;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record AssignRolesRequest(@NotEmpty Set<RoleName> roles) {
}