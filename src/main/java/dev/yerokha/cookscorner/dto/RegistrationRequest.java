package dev.yerokha.cookscorner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record RegistrationRequest(
        @NotNull @NotEmpty @Length(min = 2, max = 30, message = "Length of name must be between 2 and 30")
        String name,
        @NotNull @NotEmpty @Email
        String email,
        @NotNull @NotEmpty
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}:;.,<>/?]).{8,15}$",
        message = "Password length must be 8-15 and contains 1 upper, 1 lower and 1 special symbol")
        String password,
        @NotNull @NotEmpty
        String url
) {
}
