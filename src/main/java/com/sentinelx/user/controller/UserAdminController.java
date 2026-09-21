package com.sentinelx.user.controller;

import com.sentinelx.auth.security.UserPrincipal;
import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.user.dto.AssignRolesRequest;
import com.sentinelx.user.dto.UserResponse;
import com.sentinelx.user.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(userService.listUsers(page, size));
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<UserResponse> assignRoles(@AuthenticationPrincipal UserPrincipal actor,
                                                 @PathVariable UUID id,
                                                 @Valid @RequestBody AssignRolesRequest request) {
        return ApiResponse.ok("Roles updated", userService.assignRoles(actor.getId(), id, request.roles()));
    }
}