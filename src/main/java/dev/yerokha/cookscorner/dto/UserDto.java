package dev.yerokha.cookscorner.dto;

public record UserDto(
        Long userId,
        String name,
        String imageUrl
) {
}
