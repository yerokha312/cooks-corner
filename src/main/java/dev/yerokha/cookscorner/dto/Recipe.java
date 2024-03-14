package dev.yerokha.cookscorner.dto;

import java.util.Set;

public record Recipe(
        Long recipeId,
        String title,
        String author,
        Long authorId,
        String imageUrl,
        int cookingTimeMinutes,
        String difficulty,
        String description,
        int likes,
        int bookmarks,
        Boolean isLiked,
        Boolean isBookmarked,
        Set<Ingredient> ingredients
) {
}
