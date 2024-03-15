package dev.yerokha.cookscorner.controller;

import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.dto.LoginResponse;
import dev.yerokha.cookscorner.dto.RegistrationRequest;
import dev.yerokha.cookscorner.dto.ResetPasswordRequest;
import dev.yerokha.cookscorner.dto.SendEmailRequest;
import dev.yerokha.cookscorner.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Controller for reg/login/confirmation etc")
@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
            summary = "Registration", description = "Create a new user account",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Registration success"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "409", description = "Username or email already taken")
            }
    )
    @PostMapping("/registration")
    public ResponseEntity<String> register(@RequestBody @Valid RegistrationRequest request) {
        String email = authenticationService.createUser(request);
        return new ResponseEntity<>(String.format(
                "Confirmation link generated, email sent to %s", email), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Check availability",
            description = "Endpoint for pre-submit checking of available email. True if available",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns true or false")
            }
    )
    @PostMapping("/email-available")
    public ResponseEntity<Boolean> checkAvailable(@RequestBody @Valid @Email String email) {
        boolean emailAvailable = authenticationService.isEmailAvailable(email);
        return ResponseEntity.ok(emailAvailable);
    }

    @Operation(
            summary = "Resend mail", description = "Resend mail for user email verification",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email sent"),
                    @ApiResponse(responseCode = "400", description = "User not found"),
                    @ApiResponse(responseCode = "418", description = "User's email already verified")
            }
    )
    @PostMapping("/resend-confirmation")
    public ResponseEntity<String> resend(@RequestBody @Valid SendEmailRequest request) {
        authenticationService.sendConfirmationEmail(request.url(), request.email());
        return new ResponseEntity<>(String.format(
                "Confirmation link generated, email sent to %s", request.email()), HttpStatus.OK);
    }

    @Operation(
            summary = "Login", description = "Authenticate user and get access & refresh tokens",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Not enabled", content = @Content),
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @Operation(
            summary = "Refresh", description = "Obtain a new access token using refresh token",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Access token obtained successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid token exception", content = @Content)
            }
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }

    @Operation(
            summary = "Confirmation", description = "Confirm email by clicking the sent link ",
            tags = {"authentication", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid token exception", content = @Content)
            },
            parameters = {
                    @Parameter(name = "ct", description = "Encrypted token value", required = true)
            }

    )
    @PutMapping("/confirmation")
    public ResponseEntity<String> confirmEmail(@RequestParam("ct") String encryptedToken) {
        authenticationService.confirmEmail(encryptedToken);
        return ResponseEntity.ok("Email is confirmed");
    }

    @Operation(
            summary = "Logout", description = "Accepts plain refresh token string in body and Access token via headers " +
            "for further revocation and logging out",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Revocation and logout success"),
                    @ApiResponse(responseCode = "401", description = "Invalid token")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> revoke(@RequestBody String refreshToken, HttpServletRequest request) {
        authenticationService.revoke(refreshToken, request);
        return ResponseEntity.ok("Logout success");
    }

    @Operation(
            summary = "Forgot password", description = "Send confirmation email for password reset",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email sent or user not found")
            }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<String> findUser(@RequestBody @Valid SendEmailRequest request) {
        authenticationService.sendResetPasswordEmail(request.email(), request.url());
        return ResponseEntity.ok(String.format(
                "Confirmation link generated, email sent to %s", request.email()));
    }

    @Operation(
            summary = "Reset password", description = "Verifies received token and resets password for a new one",
            tags = {"authentication", "put"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Username from token does not match")
            }
    )
    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam("rpt") String encryptedToken,
                                                @RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request.password(), encryptedToken);
        return ResponseEntity.ok("Password reset");
    }
}
