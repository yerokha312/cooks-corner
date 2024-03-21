package dev.yerokha.cookscorner.dto;

public record UpdateCommentRequest(
        Long commentId,
        String text
) {
}
