package dev.yerokha.cookscorner.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String name
) {
}
