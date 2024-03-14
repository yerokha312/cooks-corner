package dev.yerokha.cookscorner.dto;

import lombok.Data;

@Data
public class RecipeDto {
    private Long recipeId;
    private String title;
    private String author;
    private String imageUrl;
    private int likes;
    private int bookmarks;
    private Boolean isLiked;
    private Boolean isBookmarked;

    public RecipeDto(Long recipeId, String title, String author, String imageUrl, int likes, int bookmarks) {
        this.recipeId = recipeId;
        this.title = title;
        this.author = author;
        this.imageUrl = imageUrl;
        this.likes = likes;
        this.bookmarks = bookmarks;
    }


}