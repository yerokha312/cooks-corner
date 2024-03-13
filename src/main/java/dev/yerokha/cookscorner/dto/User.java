package dev.yerokha.cookscorner.dto;

public record User(
        Long userId,
        String name,
        String bio,
        String photoUrl,
        int recipes,
        int followers,
        int following,
        Boolean isFollowed
) {
}
