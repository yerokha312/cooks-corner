package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegistrationRequest(
        @NotNull @NotEmpty
        String name,
        @NotNull @NotEmpty @Email
        String email,
        @NotNull @NotEmpty
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;.,<>/?]).{8,15}$")
        String password,
        @NotNull @NotEmpty
        String url
) {
}
