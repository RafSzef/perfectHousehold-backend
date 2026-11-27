package com.perfecthousehold.transport;

import com.perfecthousehold.security.auth.UserPrincipal;
import com.perfecthousehold.security.entity.User;
import com.perfecthousehold.security.service.AuthService;
import com.perfecthousehold.security.service.UserService;
import com.perfecthousehold.transport.request.ChangePasswordRequest;
import com.perfecthousehold.transport.request.LoginRequest;
import com.perfecthousehold.transport.response.AuthResponse;
import com.perfecthousehold.transport.view.PublicView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Registration: only authenticated users with ROLE_ADMIN can create new users.
     */
    @PublicView
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<User> register(@Validated(User.OnCreate.class) @RequestBody User user) {
        User created = authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Login: public endpoint returning JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest credentials) {
        String token = authService.login(credentials.username(), credentials.password());
        return ResponseEntity.ok(AuthResponse.of(token));
    }

    /**
     * Change current user's password (guaranteed by authenticated UserPrincipal.getId()).
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Protected profile: returns User entity with passwordHash automatically hidden.
     */
    @PublicView
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + principal.getUsername()));
        return ResponseEntity.ok(user);
    }
}
