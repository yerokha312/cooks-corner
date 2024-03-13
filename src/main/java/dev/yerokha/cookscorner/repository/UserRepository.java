package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Modifying
    @Query("UPDATE UserEntity u SET u.isEnabled = true WHERE u.email = :email")
    void enableUser(String email);

    boolean existsByUserIdAndFollowingUserId(Long userId, Long followeeId);
}
