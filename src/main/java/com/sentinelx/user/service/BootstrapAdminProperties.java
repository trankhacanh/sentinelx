package com.sentinelx.user.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinelx.bootstrap-admin")
public record BootstrapAdminProperties(String username, String email, String password) {

    /** Không để password lọt vào log nếu object bị in ra. */
    @Override
    public String toString() {
        return "BootstrapAdminProperties[username=" + username + ", email=" + email + "]";
    }
}