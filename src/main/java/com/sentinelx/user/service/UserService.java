package com.sentinelx.user.service;

import com.sentinelx.common.exception.ConflictException;
import com.sentinelx.common.exception.ResourceNotFoundException;
import com.sentinelx.common.response.PageResponse;
import com.sentinelx.user.dto.UpdateProfileRequest;
import com.sentinelx.user.dto.UserResponse;
import com.sentinelx.user.entity.Role;
import com.sentinelx.user.entity.RoleName;
import com.sentinelx.user.entity.User;
import com.sentinelx.user.repository.RoleRepository;
import com.sentinelx.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(String username, String email, String rawPassword,
                                   String fullName, Set<RoleName> roleNames) {
        String normalizedUsername = normalize(username);
        String normalizedEmail = normalize(email);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User(normalizedUsername, normalizedEmail,
                passwordEncoder.encode(rawPassword), cleanFullName(fullName), resolveRoles(roleNames));
        // Nếu 2 request trùng nhau chạy song song, cả hai cùng qua bước exists ở trên.
        // Unique constraint trong DB là chốt chặn cuối -> DataIntegrityViolationException -> 409.
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return UserResponse.from(findOrThrow(id));
    }

    @Transactional
    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = findOrThrow(id);
        String newEmail = normalize(request.email());
        if (userRepository.existsByEmailAndIdNot(newEmail, id)) {
            throw new ConflictException("Email is already registered");
        }
        user.updateProfile(newEmail, cleanFullName(request.fullName()));
        // Không cần gọi save(): entity đang "managed" trong transaction,
        // Hibernate tự phát hiện thay đổi (dirty checking) và UPDATE khi commit.
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        // Sort cố định, KHÔNG nhận sort từ client (tránh lộ dữ liệu qua thứ tự sắp xếp theo cột nhạy cảm)
        PageRequest pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        return PageResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }

    @Transactional
    public UserResponse assignRoles(UUID actorId, UUID targetId, Set<RoleName> roleNames) {
        if (actorId.equals(targetId)) {
            throw new ConflictException("You cannot change your own roles");
        }
        User target = findOrThrow(targetId);
        target.replaceRoles(resolveRoles(roleNames));
        return UserResponse.from(target);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Set<Role> resolveRoles(Set<RoleName> names) {
        List<Role> roles = roleRepository.findByNameIn(names);
        if (roles.size() != names.size()) {
            throw new IllegalStateException("Some roles are missing in the database: " + names);
        }
        return new HashSet<>(roles);
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String cleanFullName(String fullName) {
        return (fullName == null || fullName.isBlank()) ? null : fullName.trim();
    }
}