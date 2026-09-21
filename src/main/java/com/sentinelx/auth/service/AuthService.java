package com.sentinelx.auth.service;

import com.sentinelx.auth.dto.AuthResponse;
import com.sentinelx.auth.dto.LoginRequest;
import com.sentinelx.auth.dto.RegisterRequest;
import com.sentinelx.auth.security.JwtService;
import com.sentinelx.auth.security.UserPrincipal;
import com.sentinelx.user.dto.UserResponse;
import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.service.UserService;
import java.util.EnumSet;
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

    public AuthService(UserService userService, AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** Đăng ký công khai luôn chỉ cấp VIEWER (least privilege). Role không bao giờ đến từ client. */
    public UserResponse register(RegisterRequest request) {
        return userService.createUser(request.username(), request.email(), request.password(),
                request.fullName(), EnumSet.of(RoleName.VIEWER));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        } catch (BadCredentialsException | org.springframework.security.authentication.AccountStatusException ex) {
            // Cùng một thông báo cho: sai user, sai mật khẩu, tài khoản bị khóa -> không lộ user có tồn tại hay không
            throw new BadCredentialsException("Invalid username or password");
        }
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal.getId());
        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds(),
                userService.getById(principal.getId()));
    }
}