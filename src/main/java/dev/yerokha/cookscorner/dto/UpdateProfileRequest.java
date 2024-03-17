package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UpdateProfileRequest(
        @NotNull @Min(1)
        Long userId,
        @NotNull @NotEmpty @Length(min = 6, max = 30, message = "Length of name must be between 6 and 30")
        String name,
        @Length(max = 500, message = "Length of bio can not exceed 500 characters")
        String bio
) {
}
