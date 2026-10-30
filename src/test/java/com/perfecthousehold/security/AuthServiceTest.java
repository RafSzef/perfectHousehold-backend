package com.perfecthousehold.security;

import com.perfecthousehold.security.auth.UserPrincipal;
import com.perfecthousehold.security.entity.User;
import com.perfecthousehold.security.jwt.JwtTokenProvider;
import com.perfecthousehold.security.service.AuthService;
import com.perfecthousehold.security.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, tokenProvider, userService, passwordEncoder);
    }

    @Test
    void testRegisterHashesPasswordAndSaves() {
        User user = new User();
        user.setUsername("nowy_user");
        user.setEmail("nowy@example.com");
        user.setPasswordHash("surowe_haslo");

        when(userService.findByUsername("nowy_user")).thenReturn(null);
        when(passwordEncoder.encode("surowe_haslo")).thenReturn("$2a$10$hashedPassword");
        when(userService.registerUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User registered = authService.register(user);

        assertNotNull(registered);
        assertEquals("nowy_user", registered.getUsername());
        assertEquals("$2a$10$hashedPassword", registered.getPasswordHash());
        verify(userService).registerUser(user);
    }

    @Test
    void testLoginGeneratesToken() {
        Authentication auth = mock(Authentication.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        UserPrincipal principal = UserPrincipal.create(user);

        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(principal)).thenReturn("mocked.jwt.token");

        String token = authService.login("testuser", "tajne");

        assertEquals("mocked.jwt.token", token);
    }
}
