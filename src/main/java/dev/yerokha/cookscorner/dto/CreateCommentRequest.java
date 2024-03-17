package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        Long objectId,
        @NotNull @NotEmpty
        String text,
        boolean isReply
) {
}
