package dev.yerokha.cookscorner.dto;

import java.time.LocalDateTime;

public record Comment(
        Long commentId,
        Long parentCommentId,
        Long authorId,
        String profilePicture,
        String author,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        boolean isModified,
        int replyCount,
        int likeCount,
        Boolean isLiked,
        String text
) {
}
