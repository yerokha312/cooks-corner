package dev.yerokha.cookscorner.dto;

public record AccountRecoveryRequest(
        String email,
        String password,
        String url
) {
}
