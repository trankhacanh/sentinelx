package com.sentinelx.auth.service;

import com.sentinelx.audit.entity.AuditAction;
import com.sentinelx.audit.service.AuditLogService;
import com.sentinelx.auth.dto.AuthResponse;
import com.sentinelx.auth.dto.LoginRequest;
import com.sentinelx.auth.dto.RegisterRequest;
import com.sentinelx.auth.security.JwtService;
import com.sentinelx.auth.security.UserPrincipal;
import com.sentinelx.user.dto.UserResponse;
import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.service.UserService;
import java.util.EnumSet;
import java.util.Map;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthService(UserService userService, AuthenticationManager authenticationManager,
                       JwtService jwtService, AuditLogService auditLogService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    public UserResponse register(RegisterRequest request) {
        return userService.createUser(request.username(), request.email(), request.password(),
                request.fullName(), EnumSet.of(RoleName.VIEWER));
    }

    public AuthResponse login(LoginRequest request, String clientIp) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (BadCredentialsException | AccountStatusException ex) {
            auditLogService.record(null, AuditAction.USER_LOGIN_FAILED, "User", null, clientIp,
                    Map.of("attemptedUsername", request.username()));
            throw new BadCredentialsException("Invalid username or password");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getId());
        auditLogService.record(principal.getId(), AuditAction.USER_LOGIN, "User", principal.getId(), clientIp, null);
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(),
                userService.getById(principal.getId()));
    }
}