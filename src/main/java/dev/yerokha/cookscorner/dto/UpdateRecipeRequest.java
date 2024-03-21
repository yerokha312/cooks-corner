package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UpdateRecipeRequest(
        @NotNull @Min(1)
        Long recipeId,
        @NotNull @NotEmpty @Length(min = 3, max = 40, message = "Title length must be between 3 and 40")
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
