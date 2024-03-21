package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.Category;
import dev.yerokha.cookscorner.entity.RecipeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryNameIgnoreCase(String name);
    Page<RecipeEntity> findAllByCategoryId(byte categoryId, Pageable pageable);
}
