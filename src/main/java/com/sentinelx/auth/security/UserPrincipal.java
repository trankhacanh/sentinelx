package com.sentinelx.auth.security;

import com.sentinelx.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Đại diện người dùng đã xác thực trong SecurityContext. Không phải JPA entity. */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final List<SimpleGrantedAuthority> authorities;

    private UserPrincipal(UUID id, String username, String passwordHash, boolean enabled,
                          List<SimpleGrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        // hasRole('ADMIN') của Spring Security tự thêm tiền tố "ROLE_" khi so khớp
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                .toList();
        return new UserPrincipal(user.getId(), user.getUsername(), user.getPasswordHash(),
                user.isEnabled(), authorities);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}