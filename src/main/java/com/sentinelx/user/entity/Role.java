package com.sentinelx.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/** Role là dữ liệu tham chiếu do Flyway seed, ứng dụng không tạo role mới. */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RoleName name;

    protected Role() {
        // JPA yêu cầu constructor không tham số
    }

    public UUID getId() {
        return id;
    }

    public RoleName getName() {
        return name;
    }
}