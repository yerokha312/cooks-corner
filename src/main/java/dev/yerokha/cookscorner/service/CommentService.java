package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.Comment;
import dev.yerokha.cookscorner.dto.CreateCommentRequest;
import dev.yerokha.cookscorner.dto.UpdateCommentRequest;
import dev.yerokha.cookscorner.entity.CommentEntity;
import dev.yerokha.cookscorner.exception.ForbiddenException;
import dev.yerokha.cookscorner.exception.NotFoundException;
import dev.yerokha.cookscorner.repository.CommentRepository;
import dev.yerokha.cookscorner.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static dev.yerokha.cookscorner.mapper.CommentMapper.toComment;
import static java.lang.Integer.parseInt;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeService recipeService;
    private final UserService userService;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, RecipeService recipeService, UserService userService, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.recipeService = recipeService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Comment createComment(CreateCommentRequest request, Long userIdFromAuthToken) {
        CommentEntity entity = toEntity(request, userIdFromAuthToken);

        return toComment(commentRepository.save(entity),
                userIdFromAuthToken);
    }

    private CommentEntity toEntity(CreateCommentRequest request, Long userIdFromAuthToken) {
        CommentEntity entity = new CommentEntity();
        entity.setCreatedAt(LocalDateTime.now());
        if (request.isReply()) {
            CommentEntity comment = commentRepository.findById(request.objectId()).orElseThrow(
                    () -> new NotFoundException("Comment not found"));
            entity.setParentComment(comment);
        } else {
            entity.setRecipeEntity(recipeService.getRecipeById(request.objectId()));
        }

        entity.setAuthor(userService.getUserEntityById(userIdFromAuthToken));
        entity.setText(request.text());
        return entity;
    }

    public Page<Comment> getComments(Long objectId, Map<String, String> params, Long userIdFromAuthToken) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "5"))
        );

        return commentRepository.findAllByRecipeEntityRecipeIdAndParentCommentIsNull(objectId, pageable)
                .map(entity -> toComment(entity, userIdFromAuthToken));
    }

    public Page<Comment> getReplies(Long parentId, Map<String, String> params, Long userIdFromAuthToken) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "5"))
        );

        return commentRepository.findAllByParentCommentCommentId(parentId, pageable)
                .map(entity -> toComment(entity, userIdFromAuthToken));
    }

    public Comment updateComment(UpdateCommentRequest request, Long userIdFromAuthToken) {
        boolean exists = userRepository.existsById(userIdFromAuthToken);

        if (!exists) {
            throw new NotFoundException("User not found");
        }

        CommentEntity comment = commentRepository.findById(
                request.commentId()).orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!userIdFromAuthToken.equals(comment.getAuthor().getUserId())) {
            throw new ForbiddenException("User is not the author of this comment");
        }

        comment.setText(request.text());
        comment.setUpdatedAt(LocalDateTime.now());

        return toComment(comment, userIdFromAuthToken);
    }
}































