package dev.yerokha.cookscorner.repository;

import dev.yerokha.cookscorner.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    Page<CommentEntity> findAllByRecipeEntityRecipeIdAndParentCommentIsNull(Long recipeId, Pageable pageable);
    Page<CommentEntity> findAllByParentCommentCommentId(Long parentCommentId, Pageable pageable);
}
