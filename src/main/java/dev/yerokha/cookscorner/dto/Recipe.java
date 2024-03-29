package dev.yerokha.cookscorner.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record Recipe(
        Long recipeId,
        LocalDateTime lastUpdated,
        String title,
        String author,
        Long authorId,
        String imageUrl,
        int cookingTimeMinutes,
        String difficulty,
        String description,
        int likes,
        int bookmarks,
        int comments,
        Boolean isLiked,
        Boolean isBookmarked,
        Set<Ingredient> ingredients
) {
}
