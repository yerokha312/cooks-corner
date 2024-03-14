package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.RecipeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

    Page<RecipeEntity> findByCategory_CategoryName(String categoryName, Pageable pageable);

    Page<RecipeEntity> findByUserEntityUserId(Long userId, Pageable pageable);
    Page<RecipeEntity> findByBookmarksUserId(Long userId, Pageable pageable);
    Page<RecipeEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);
}
