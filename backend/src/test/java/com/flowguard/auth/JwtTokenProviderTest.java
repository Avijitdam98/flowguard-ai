package com.flowguard.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String secret = "4c9a632e8f1b4021a8d052d9b671f30c5e7b2a9d8c3e4f1a2b5d6e7f8a9b0c1d";
    private final long accessExpiration = 3600000; // 1 hour
    private final long refreshExpiration = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(secret, accessExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("Should generate valid access token and extract claims accurately")
    void testGenerateAccessTokenAndValidate() {
        UUID userId = UUID.randomUUID();
        String email = "engineer@flowguard.ai";
        UserPrincipal principal = new UserPrincipal(
                userId,
                email,
                "passwordHash",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ENGINEER"))
        );
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String token = tokenProvider.generateAccessToken(auth);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(userId, tokenProvider.getUserIdFromToken(token));
        assertEquals(email, tokenProvider.getEmailFromToken(token));
        assertEquals("ACCESS", tokenProvider.getTokenType(token));
    }

    @Test
    @DisplayName("Should generate valid refresh token with REFRESH token type")
    void testGenerateRefreshToken() {
        UUID userId = UUID.randomUUID();
        String email = "admin@flowguard.ai";

        String refreshToken = tokenProvider.generateRefreshToken(userId, email);

        assertNotNull(refreshToken);
        assertTrue(tokenProvider.validateToken(refreshToken));
        assertEquals(userId, tokenProvider.getUserIdFromToken(refreshToken));
        assertEquals("REFRESH", tokenProvider.getTokenType(refreshToken));
    }

    @Test
    @DisplayName("Should reject malformed or tampered token")
    void testValidateMalformedToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
        assertFalse(tokenProvider.validateToken(""));
    }
}

