package com.perfecthousehold.security;

import com.perfecthousehold.generic.exception.ConflictException;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

        when(userService.findByUsername("nowy_user")).thenReturn(Optional.empty());
        when(userService.findByEmail("nowy@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("surowe_haslo")).thenReturn("$2a$10$hashedPassword");
        when(userService.registerUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User registered = authService.register(user);

        assertNotNull(registered);
        assertEquals("nowy_user", registered.getUsername());
        assertEquals("$2a$10$hashedPassword", registered.getPasswordHash());
        verify(userService).registerUser(user);
    }

    @Test
    void testRegisterAlwaysEncodesEvenIfPasswordLooksLikeBcryptPrefix() {
        User user = new User();
        user.setUsername("nowy_user");
        user.setEmail("nowy@example.com");
        user.setPasswordHash("$2a$fakePrefix");

        when(userService.findByUsername("nowy_user")).thenReturn(Optional.empty());
        when(userService.findByEmail("nowy@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("$2a$fakePrefix")).thenReturn("$2a$10$realEncoded");
        when(userService.registerUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User registered = authService.register(user);

        assertEquals("$2a$10$realEncoded", registered.getPasswordHash());
        verify(passwordEncoder).encode("$2a$fakePrefix");
    }

    @Test
    void testRegisterFailsWithConflictWhenUsernameTaken() {
        User user = new User();
        user.setUsername("istniejacy");
        user.setEmail("nowy@example.com");
        user.setPasswordHash("tajne123");

        when(userService.findByUsername("istniejacy")).thenReturn(Optional.of(new User()));

        ConflictException ex = assertThrows(ConflictException.class, () -> authService.register(user));
        assertTrue(ex.getMessage().contains("istniejacy"));
        verify(userService, never()).registerUser(any());
    }

    @Test
    void testRegisterFailsWithConflictWhenEmailTaken() {
        User user = new User();
        user.setUsername("nowy_user");
        user.setEmail("zajety@example.com");
        user.setPasswordHash("tajne123");

        when(userService.findByUsername("nowy_user")).thenReturn(Optional.empty());
        when(userService.findByEmail("zajety@example.com")).thenReturn(Optional.of(new User()));

        ConflictException ex = assertThrows(ConflictException.class, () -> authService.register(user));
        assertTrue(ex.getMessage().contains("zajety@example.com"));
        verify(userService, never()).registerUser(any());
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

    @Test
    void testChangePasswordSuccess() {
        User user = new User();
        user.setId(5L);
        user.setUsername("kasia");
        user.setPasswordHash("$2a$10$oldHashedPassword");

        when(userService.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("stareHaslo", "$2a$10$oldHashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("noweHaslo123")).thenReturn("$2a$10$newHashedPassword");

        assertDoesNotThrow(() -> authService.changePassword(5L, "stareHaslo", "noweHaslo123"));
        verify(userService).updatePassword(5L, "$2a$10$newHashedPassword");
    }

    @Test
    void testChangePasswordFailsWhenUserNotFound() {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                authService.changePassword(999L, "stareHaslo", "noweHaslo123")
        );
        verify(userService, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void testChangePasswordFailsWhenCurrentPasswordMismatch() {
        User user = new User();
        user.setId(5L);
        user.setPasswordHash("$2a$10$oldHashedPassword");

        when(userService.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("zleHaslo", "$2a$10$oldHashedPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.changePassword(5L, "zleHaslo", "noweHaslo123")
        );
        assertEquals("Current password does not match", ex.getMessage());
        verify(userService, never()).updatePassword(anyLong(), anyString());
    }
}
