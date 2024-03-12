package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.dto.LoginRequest;
import dev.yerokha.cookscorner.dto.LoginResponse;
import dev.yerokha.cookscorner.dto.RegistrationRequest;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.exception.EmailAlreadyTakenException;
import dev.yerokha.cookscorner.exception.UserAlreadyEnabledException;
import dev.yerokha.cookscorner.repository.RoleRepository;
import dev.yerokha.cookscorner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, TokenService tokenService, MailService mailService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
    }


    public void createUser(RegistrationRequest request) {
        String email = request.email();
        if (!isEmailAvailable(email)) {
            throw new EmailAlreadyTakenException(String.format("Email %s already taken", email));
        }
        UserEntity entity = new UserEntity(
                request.name(),
                email.toLowerCase(),
                passwordEncoder.encode(request.password()),
                Set.of(roleRepository.findByAuthority("USER"))
        );

        userRepository.save(entity);

        sendConfirmationEmail(request.url(), email);
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }


    public void sendConfirmationEmail(String url, String email) {
        UserEntity entity = userRepository.findByEmail(
                email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (entity.isEnabled()) {
            throw new UserAlreadyEnabledException("User has already confirmed email address");
        }
        String confirmationToken = tokenService.generateConfirmationToken(entity);
        mailService.sendConfirmationEmail(entity.getEmail(), url + "?ct=" + confirmationToken);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()));
            UserEntity entity = (UserEntity) authentication.getPrincipal();
            return new LoginResponse(
                    tokenService.generateAccessToken(entity),
                    tokenService.generateRefreshToken(entity)
            );

        } catch (AuthenticationException e) {
            if (e instanceof DisabledException) {
                throw new DisabledException("Account has not been enabled");
            } else {
                throw new BadCredentialsException("Invalid username or password");
            }
        }
    }

    public String refreshToken(String refreshToken) {
        return tokenService.refreshAccessToken(refreshToken);
    }

    @Transactional
    public void confirmEmail(String encryptedToken) {
        String email = tokenService.confirmationTokenIsValid(encryptedToken);
        userRepository.enableUser(email);
    }

    public void revoke(String refreshToken, HttpServletRequest request) {
        final String accessToken = request.getHeader("Authorization");
        tokenService.revokeToken(accessToken);
        tokenService.revokeToken(refreshToken);
    }

    public void sendResetPasswordEmail(String email, String url) {
        UserEntity entity = userRepository.findByEmail(email).orElse(null);
        if (entity == null) {
            return;
        }
        String confirmationToken = tokenService.generateConfirmationToken(entity);
        mailService.sendConfirmationEmail(entity.getEmail(),
                (url + "?rpt=" + confirmationToken));
    }

    public void resetPassword(String password, String encryptedToken) {
        String email = tokenService.confirmationTokenIsValid(encryptedToken);
        UserEntity entity = userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        entity.setPassword(passwordEncoder.encode(password));
        tokenService.revokeAllRefreshTokes(email);
        userRepository.save(entity);
    }

}




























