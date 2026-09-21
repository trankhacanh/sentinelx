package com.sentinelx.user.repository;

import com.sentinelx.user.entity.Role;
import com.sentinelx.user.entity.RoleName;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findByNameIn(Collection<RoleName> names);
}