package com.sentinelx.user.controller;

import com.sentinelx.auth.security.UserPrincipal;
import com.sentinelx.common.response.ApiResponse;
import com.sentinelx.user.dto.UpdateProfileRequest;
import com.sentinelx.user.dto.UserResponse;
import com.sentinelx.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        // Id lấy từ SecurityContext (đã xác thực), không bao giờ từ client
        return ApiResponse.ok(userService.getById(principal.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", userService.updateProfile(principal.getId(), request));
    }
}