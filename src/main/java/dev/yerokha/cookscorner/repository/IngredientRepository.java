package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.IngredientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

    Optional<IngredientEntity> findByIngredientNameIgnoreCase(String name);
}
