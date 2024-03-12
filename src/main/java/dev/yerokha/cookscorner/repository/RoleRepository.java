package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByAuthority(String authority);
}

