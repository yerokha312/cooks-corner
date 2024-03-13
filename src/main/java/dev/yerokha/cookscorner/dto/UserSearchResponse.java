package dev.yerokha.cookscorner.dto;

public record UserSearchResponse(
        Long userId,
        String name,
        String imageUrl
) {
}
