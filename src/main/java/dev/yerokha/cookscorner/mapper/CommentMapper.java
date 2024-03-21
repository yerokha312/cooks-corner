package dev.yerokha.cookscorner.mapper;

import dev.yerokha.cookscorner.dto.Comment;
import dev.yerokha.cookscorner.entity.CommentEntity;

public class CommentMapper {

    public static Comment toComment(CommentEntity entity, Long userIdFromAuthentication) {
        Boolean isLiked = null;
        if (userIdFromAuthentication != null) {
            isLiked = entity.getLikes().stream().anyMatch(
                    like -> like.getUserId().equals(userIdFromAuthentication));
        }
        return new Comment(
                entity.getCommentId(),
                entity.getParentComment() == null ? null : entity.getParentComment().getCommentId(),
                entity.getAuthor().getUserId(),
                entity.getAuthor().getProfilePicture() == null ?
                        null : entity.getAuthor().getProfilePicture().getImageUrl(),
                entity.getAuthor().getName(),
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
