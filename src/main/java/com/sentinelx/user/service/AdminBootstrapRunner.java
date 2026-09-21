package com.sentinelx.user.service;

import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.repository.UserRepository;
import java.util.EnumSet;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Tạo ADMIN đầu tiên khi khởi động, chỉ nếu BOOTSTRAP_ADMIN_PASSWORD được cấu hình. */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final UserService userService;

    public AdminBootstrapRunner(BootstrapAdminProperties properties, UserRepository userRepository,
                                UserService userService) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String password = properties.password();
        if (password == null || password.isBlank()) {
            log.info("Bootstrap admin is disabled (BOOTSTRAP_ADMIN_PASSWORD is not set)");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (isBlank(properties.username()) || isBlank(properties.email())) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_EMAIL are required");
        }

        String username = properties.username().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username)) {
            log.info("Bootstrap admin '{}' already exists, skipping", username);
            return;
        }

        userService.createUser(username, properties.email(), password,
                "Bootstrap Admin", EnumSet.of(RoleName.ADMIN));
        log.warn("Bootstrap admin '{}' created. Remove BOOTSTRAP_ADMIN_PASSWORD from the environment "
                + "after first login.", username);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}