package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE (LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :bio, '%'))) " +
            "AND u.deleted = false AND u.enabled = true " +
            "ORDER BY SIZE(u.followers) DESC")
    Page<UserEntity> findByNameOrBioSortedByFollowersCount(
            String name, String bio, Pageable pageable);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE u.deleted = false AND u.enabled = true " +
            "ORDER BY SIZE(u.followers) DESC")
    Page<UserEntity> findAllSortedByFollowersCount(Pageable pageable);

    @Modifying
    @Query("UPDATE UserEntity u SET u.enabled = true WHERE u.email = :email")
    void enableUser(String email);

    boolean existsByUserIdAndFollowingUserId(Long userId, Long followeeId);

    Boolean existsByUserIdAndLikedRecipes_RecipeId(Long userIdFromAuthToken, Long recipeId);

    Boolean existsByUserIdAndBookmarkedRecipes_RecipeId(Long userIdFromAuthToken, Long recipeId);

    Page<UserEntity> findByFollowingUserId(Long userId, Pageable pageable);

    Page<UserEntity> findByFollowersUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE UserEntity u SET u.viewCount = u.viewCount + 1 WHERE u.userId = :userId")
    void incrementViewCount(Long userId);

    @Query("SELECT u.viewCount FROM UserEntity u WHERE u.userId = :userId")
    long getViewCount(Long userId);
}
