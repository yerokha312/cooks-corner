package dev.yerokha.cookscorner.dto;

public record UpdateProfileResponse(
        Long userId,
        String name,
        String bio,
        String imageUrl
) {
}
