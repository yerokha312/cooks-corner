package dev.yerokha.cookscorner.dto;

import java.time.LocalDateTime;

public record Comment(
        Long commentId,
        Long parentCommentId,
        Long authorId,
        String imageUrl,
        String author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isUpdated,
        int replyCount,
        int likeCount,
        Boolean isLiked,
        String text
) {
}
