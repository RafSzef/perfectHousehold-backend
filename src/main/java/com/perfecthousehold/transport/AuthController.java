package com.perfecthousehold.transport;

import com.perfecthousehold.security.auth.UserPrincipal;
import com.perfecthousehold.security.entity.User;
import com.perfecthousehold.security.service.AuthService;
import com.perfecthousehold.security.service.UserService;
import com.perfecthousehold.transport.request.LoginRequest;
import com.perfecthousehold.transport.response.AuthResponse;
import com.perfecthousehold.transport.view.PublicView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Registration: accepts User directly from JSON body, returns created User (passwordHash hidden).
     */
    @PublicView
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        User created = authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Login: accepts strongly-typed LoginRequest, returns AuthResponse with token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest credentials) {
        String token = authService.login(credentials.username(), credentials.password());
        return ResponseEntity.ok(AuthResponse.of(token));
    }

    /**
     * Protected profile: returns User entity with passwordHash automatically hidden.
     */
    @PublicView
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findByUsername(principal.getUsername());
        return ResponseEntity.ok(user);
    }
}
