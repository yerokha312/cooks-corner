package dev.yerokha.cookscorner.service;

import dev.yerokha.cookscorner.entity.RefreshToken;
import dev.yerokha.cookscorner.entity.UserEntity;
import dev.yerokha.cookscorner.enums.TokenType;
import dev.yerokha.cookscorner.exception.InvalidTokenException;
import dev.yerokha.cookscorner.repository.TokenRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.yerokha.cookscorner.util.RedisUtil.containsKey;
import static dev.yerokha.cookscorner.util.RedisUtil.deleteKey;
import static dev.yerokha.cookscorner.util.RedisUtil.getValue;
import static dev.yerokha.cookscorner.util.RedisUtil.setValue;
import static dev.yerokha.cookscorner.util.TokenEncryptionUtil.decryptToken;
import static dev.yerokha.cookscorner.util.TokenEncryptionUtil.encryptToken;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenRepository tokenRepository;
    private static final int expirationMinutes = 5;
    private static final int ACCESS_TOKEN_EXPIRATION = expirationMinutes * 3;
    private static final int REFRESH_TOKEN_EXPIRATION = expirationMinutes * 12 * 24 * 7;

    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, TokenRepository tokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.tokenRepository = tokenRepository;
    }

    public String generateConfirmationToken(UserEntity entity) {
        String encryptedToken = encryptToken("Bearer " + generateToken(entity, expirationMinutes, TokenType.CONFIRMATION));
        String key = "confirmation_token:" + entity.getUsername();
        setValue(key, encryptedToken, expirationMinutes, TimeUnit.MINUTES);
        return encryptedToken;
    }

    public String confirmationTokenIsValid(String encryptedToken) {
        String confirmationToken = decryptToken(encryptedToken);
        Jwt decodedToken = decodeToken(confirmationToken);
        String email = decodedToken.getSubject();
        String key = "confirmation_token:" + email;
        boolean isValid = containsKey(key);
        if (!isValid || !encryptedToken.equals(getValue(key))) {
            throw new InvalidTokenException("Confirmation link is expired");
        }
        deleteKey(key);
        return email;
    }

    public String generateAccessToken(UserEntity entity) {
        String accessToken = generateToken(entity, ACCESS_TOKEN_EXPIRATION, TokenType.ACCESS);
        setValue("access_token:" + entity.getUsername(),
                encryptToken(accessToken),
                ACCESS_TOKEN_EXPIRATION,
                TimeUnit.MINUTES);
        return accessToken;
    }

    public String generateRefreshToken(UserEntity entity) {
        String token = generateToken(entity, REFRESH_TOKEN_EXPIRATION, TokenType.REFRESH);
        String encryptedToken = encryptToken("Bearer " + token);
        RefreshToken refreshToken = new RefreshToken(
                encryptedToken,
                entity,
                Instant.now(),
                Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.MINUTES)
        );
        tokenRepository.save(refreshToken);
        return token;
    }

    private String generateToken(UserEntity entity, int expirationTime, TokenType tokenType) {
        Instant now = Instant.now();
        String scopes = getScopes(entity);

        JwtClaimsSet claims = getClaims(now, expirationTime, entity.getUsername(), scopes, tokenType);
        return encodeToken(claims);
    }

    private String getScopes(UserEntity entity) {
        return entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    private JwtClaimsSet getClaims(Instant now, long expirationTime, String subject, String scopes, TokenType tokenType) {
        return JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expirationTime, ChronoUnit.MINUTES))
                .subject(subject)
                .claim("scopes", scopes)
                .claim("tokenType", tokenType)
                .build();
    }

    private String encodeToken(JwtClaimsSet claims) {
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getEmailFromToken(String token) {
        return decodeToken(token).getSubject();
    }

    private Jwt decodeToken(String token) {
        if (!token.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid token format");
        }

        String strippedToken = token.substring(7);

        try {
            return jwtDecoder.decode(strippedToken);
        } catch (Exception e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    public String refreshAccessToken(String refreshToken) {
        Jwt decodedToken = decodeToken(refreshToken);
        String email = decodedToken.getSubject();
        if (!decodedToken.getClaim("tokenType").equals(TokenType.REFRESH.name())) {
            throw new InvalidTokenException("Invalid token type");
        }

        if (isExpired(decodedToken)) {
            throw new InvalidTokenException("Refresh token expired");
        }

        if (isRevoked(refreshToken, email)) {
            throw new InvalidTokenException("Token is revoked");
        }

        Instant now = Instant.now();
        String subject = decodedToken.getSubject();
        String scopes = decodedToken.getClaim("scopes");
        JwtClaimsSet claims = getClaims(now, ACCESS_TOKEN_EXPIRATION, subject, scopes, TokenType.ACCESS);
        String token = encodeToken(claims);
        String key = "access_token:" + email;
        setValue(key, encryptToken(token), ACCESS_TOKEN_EXPIRATION, TimeUnit.MINUTES);
        return token;

    }

    private boolean isRevoked(String refreshToken, String email) {
        List<RefreshToken> tokenList = tokenRepository.findNotRevokedByEmail(email);
        if (tokenList.isEmpty()) {
            return true;
        }

        for (RefreshToken token : tokenList) {
            if (refreshToken.equals(decryptToken(token.getToken()))) {
                return false;
            }
        }

        return true;
    }

    private boolean isExpired(Jwt decodedToken) {
        return Objects.requireNonNull(decodedToken.getExpiresAt()).isBefore(Instant.now());
    }

    public void revokeToken(String token) {
        String email = decodeToken(token).getSubject();
        String key = "access_token:" + email;
        if (containsKey(key)) {
            deleteKey(key);
            return;
        }
        List<RefreshToken> notRevokedByUsername = tokenRepository.findNotRevokedByEmail(email);
        for (RefreshToken refreshToken : notRevokedByUsername) {
            if (token.equals(decryptToken(refreshToken.getToken()))) {
                refreshToken.setRevoked(true);
                tokenRepository.save(refreshToken);
                return;
            }
        }
    }

    public void revokeAllRefreshTokes(String email) {
        List<RefreshToken> notRevokedByUsername = tokenRepository.findNotRevokedByEmail(email);
        notRevokedByUsername.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(notRevokedByUsername);
    }
}
