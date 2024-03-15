package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.Email;

public record SendEmailRequest(
        @Email
        String email,
        String url
) {
}
