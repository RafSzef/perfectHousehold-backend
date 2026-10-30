package com.perfecthousehold.security.jwt;

import com.perfecthousehold.security.auth.UserPrincipal;
import com.perfecthousehold.security.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void testGenerateAndValidateToken() {
        User user = new User();
        user.setId(42L);
        user.setUsername("jan_kowalski");
        user.setEmail("jan@example.com");
        user.setRole("ROLE_ADMIN");
        user.setEnabled(true);

        UserPrincipal principal = UserPrincipal.create(user);

        String token = tokenProvider.generateToken(principal);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("jan_kowalski", tokenProvider.getUsernameFromJwt(token));
        assertEquals(42L, tokenProvider.getUserIdFromJwt(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));
    }
}
