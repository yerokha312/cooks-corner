package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.RecipeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

    Page<RecipeEntity> findAllByCategoryCategoryId(byte categoryId, Pageable pageable);
    Page<RecipeEntity> findByUserEntityUserId(Long userId, Pageable pageable);
    Page<RecipeEntity> findByBookmarksUserId(Long userId, Pageable pageable);
    Page<RecipeEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);

    @Modifying
    @Query("UPDATE RecipeEntity r SET r.viewCount = r.viewCount + 1 WHERE r.recipeId = :recipeId")
    void incrementViewCount(Long recipeId);

    @Query("SELECT r.viewCount FROM RecipeEntity r WHERE r.recipeId = :recipeId")
    long getViewCount(long recipeId);
}
