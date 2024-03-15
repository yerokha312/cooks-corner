package dev.yerokha.cookscorner.mapper;

import dev.yerokha.cookscorner.dto.RecipeDto;
import dev.yerokha.cookscorner.entity.RecipeEntity;

public class RecipeMapper {

    public static RecipeDto toRecipeDto(RecipeEntity entity) {
        return new RecipeDto(
                entity.getRecipeId(),
                entity.getTitle(),
                entity.getUserEntity().getName(),
                entity.getImage() == null ? null : entity.getImage().getImageUrl(),
                entity.getLikes().size(),
                entity.getBookmarks().size()
        );
    }
}
