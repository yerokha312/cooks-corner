package dev.yerokha.cookscorner.mapper;

import dev.yerokha.cookscorner.dto.Comment;
import dev.yerokha.cookscorner.entity.CommentEntity;
import dev.yerokha.cookscorner.entity.Image;
import dev.yerokha.cookscorner.entity.UserEntity;

public class CommentMapper {

    public static Comment toComment(CommentEntity entity, Long userIdFromAuthentication) {
        Boolean isLiked = null;
        if (userIdFromAuthentication != null) {
            isLiked = entity.getLikes().stream().anyMatch(
                    like -> like.getUserId().equals(userIdFromAuthentication));
        }

        UserEntity author = entity.getAuthor();
        boolean deleted = author.isDeleted();
        Long userId = deleted ? null : author.getUserId();
        Image profilePicture = author.getProfilePicture();
        String imageUrl = deleted ? null : (profilePicture == null ? null : profilePicture.getImageUrl());
        String name = deleted ? "Deleted User" : author.getName();

        return new Comment(
                entity.getCommentId(),
                entity.getParentComment() == null ? null : entity.getParentComment().getCommentId(),
                userId,
                imageUrl,
                name,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getUpdatedAt() != null,
                entity.getReplies().size(),
                entity.getLikes().size(),
                isLiked,
                entity.getText()
        );
    }
}
