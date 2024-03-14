package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record CreateRecipeRequest(
        @NotNull @NotEmpty @Length(min = 3, max = 30, message = "Length must be between 3 and 30")
        String title,
        @Positive
        int cookingTimeMinutes,
        @NotNull @NotEmpty
        String difficulty,
        @Length(max = 1000, message = "Description can not exceed 1000 characters")
        String description,
        @NotNull @NotEmpty
        String category,
        @NotNull @NotEmpty @Size(min = 1)
        Set<Ingredient> ingredients
        ) {
}
