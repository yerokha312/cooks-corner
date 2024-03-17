package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreateCommentRequest(
        @NotNull @Min(1)
        Long objectId,
        @NotNull @NotEmpty @Length(max = 255, message = "Comment must not exceed 255 char")
        String text,
        boolean isReply
) {
}
