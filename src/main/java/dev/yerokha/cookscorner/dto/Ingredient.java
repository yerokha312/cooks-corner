package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Ingredient(
        @NotNull @NotEmpty
        String ingredient,
        String amount,
        String measureUnit
) {
}
