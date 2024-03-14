package dev.yerokha.cookscorner.dto;

public record Ingredient(
        String ingredient,
        double amount,
        String measureUnit
) {
}
