package com.perfecthousehold.security.service;

import com.perfecthousehold.security.auth.UserPrincipal;
import com.perfecthousehold.security.entity.User;
import com.perfecthousehold.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user. Only admins can create users.
     */
    @Transactional
    public User register(User user) {
        if (userService.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username is already taken: " + user.getUsername());
        }

        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        return userService.registerUser(user);
    }

    /**
     * Authenticates user credentials and generates a JWT access token.
     */
    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return tokenProvider.generateToken(principal);
    }

    /**
     * Changes current user's password, strictly querying and updating by authenticated user ID.
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }

        User user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        userService.updatePassword(userId, encodedNewPassword);
        log.info("Password successfully changed for userId: {}", userId);
    }
}
